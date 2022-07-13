package tui

import tui.TerminalApp.Step
import view.View.string2View
import view.*
import tui.components.{Choose, LineInput}
import zio.stream.*
import zio.*

trait TerminalApp[-I, S, +A] { self =>
  def run(initialState: S): RIO[TUI, A] =
    runOption(initialState).map(_.get)

  def runOption(initialState: S): RIO[TUI, Option[A]] =
    TUI.run(self)(initialState)

  def render(state: S): View

  def update(state: S, event: TerminalEvent[I]): Step[S, A]
}

object TerminalApp {
  sealed trait Step[+S, +A]

  object Step {
    def update[S](state: S): Step[S, Nothing]   = Update(state)
    def succeed[A](result: A): Step[Nothing, A] = Done(result)
    def exit: Step[Nothing, Nothing]            = Exit

    private[tui] case class Update[S](state: S) extends Step[S, Nothing]
    private[tui] case class Done[A](result: A)  extends Step[Nothing, A]
    private[tui] case object Exit               extends Step[Nothing, Nothing]
  }
}

sealed trait TerminalEvent[+I]

trait TUI {
  def run[I, S, A](
    terminalApp: TerminalApp[I, S, A],
    events: ZStream[Any, Throwable, I],
    initialState: S
  ): Task[Option[A]]
}

object TUI {

  def live(fullScreen: Boolean): ZLayer[Any, Nothing, TUI] =
    ZLayer {
      for {
        map <- Ref.make(TextMap.ofDim(0, 0))
      } yield TUILive(fullScreen, map)
    }

  def run[I, S, A](terminalApp: TerminalApp[I, S, A])(initialState: S): RIO[TUI, Option[A]] =
    ZIO.serviceWithZIO[TUI](_.run(terminalApp, ZStream.never, initialState))

  def runWithEvents[I, S, A](
    terminalApp: TerminalApp[I, S, A]
  )(events: ZStream[Any, Throwable, I], initialState: S): RIO[TUI, Option[A]] =
    ZIO.serviceWithZIO[TUI](_.run(terminalApp, events, initialState))
}

case class TUILive(
  fullScreen: Boolean,
  oldMap: Ref[TextMap]
) extends TUI {

  @volatile
  var lastHeight = 0

  def run[I, S, A](
    terminalApp: TerminalApp[I, S, A],
    events: ZStream[Any, Throwable, I],
    initialState: S
  ): Task[Option[A]] =
    ZIO.scoped {
      for {
        _             <- Input.rawModeScoped(fullScreen)
        stateRef      <- SubscriptionRef.make(initialState)
        resultPromise <- Promise.make[Nothing, Option[A]]
        _ <- (for {
               _               <- ZIO.succeed(Input.ec.clear())
               (width, height) <- ZIO.succeed(Input.terminalSize)
               _               <- renderFullScreen(terminalApp, initialState, width, height)
             } yield ()).when(fullScreen)

        renderStream =
          stateRef.changes
            .zipWithLatest(Input.terminalSizeStream)((_, _))
            .tap { case (state, (width, height)) =>
              if (fullScreen) renderFullScreen(terminalApp, state, width, height)
              else renderTerminal(terminalApp, state)
            }

        updateStream = Input.keyEventStream.mergeEither(events).tap { keyEvent =>
                         val event = keyEvent match {
                           case Left(value)  => TerminalEvent.SystemEvent(value)
                           case Right(value) => TerminalEvent.UserEvent(value)
                         }

                         stateRef.updateZIO { state =>
                           terminalApp.update(state, event) match {
                             case Step.Update(state) => ZIO.succeed(state)
                             case Step.Done(result)  => resultPromise.succeed(Some(result)).as(state)
                             case Step.Exit          => resultPromise.succeed(None).as(state)
                           }
                         }
                       }

        _      <- ZStream.mergeAllUnbounded()(renderStream, updateStream).interruptWhen(resultPromise.await).runDrain
        result <- resultPromise.await
      } yield result
    }

  private val escape               = "\u001b["
  private val clearToEndAnsiString = s"$escape${"0J"}"

  def renderFullScreen[I, S, A](
    terminalApp: TerminalApp[I, S, A],
    state: S,
    width: Int,
    height: Int
  ): UIO[Unit] =
    ZIO.succeed {
      val map = terminalApp.render(state).center.textMap(width, height)
      print(clearToEndAnsiString + map.toString)
    }

  private def renderTerminal[I, S, A]( //
    terminalApp: TerminalApp[I, S, A],
    state: S
  ): UIO[Unit] =
    ZIO.succeed {
//    oldMap.update { map =>
//      val newMap = terminalApp.render(state).renderTextMap
//      val rendered = TextMap.diff(map, newMap)
//      println(rendered)
//      lastHeight = newMap.height
//      newMap

      val newMap   = terminalApp.render(state).renderTextMap
      val rendered = newMap.toString
      println(scala.Console.RESET + TextMap.moveUp(lastHeight) + clearToEndAnsiString + rendered + scala.Console.RESET)
      lastHeight = newMap.height
    }
}

object TerminalEvent {
  case class UserEvent[+I](event: I)         extends TerminalEvent[I]
  case class SystemEvent(keyEvent: KeyEvent) extends TerminalEvent[Nothing]
}
