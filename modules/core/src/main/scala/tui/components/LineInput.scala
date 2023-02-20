package tui.components

import tui.{TUI, TerminalApp, TerminalEvent}
import tui.TerminalApp.Step
import tui.view.View.string2View
import tui.view.{KeyEvent, View}
import zio.*

case class LineInput(prompt: String) extends TerminalApp[Any, String, String] {

  override def render(state: String): View =
    View.horizontal(0)(s"$prompt".bold.green, state.bold, View.text(" ").reversed.blinking)

  override def update(state: String, event: TerminalEvent[Any]): Task[Step[String, String]] =
    event match {
      case TerminalEvent.SystemEvent(KeyEvent.Character(c)) => ZIO.succeed(Step.update(state + c))
      case TerminalEvent.SystemEvent(KeyEvent.Delete)       => ZIO.succeed(Step.update(state.dropRight(1)))
      case TerminalEvent.SystemEvent(KeyEvent.Exit)         => ZIO.succeed(Step.exit)
      case TerminalEvent.SystemEvent(KeyEvent.Enter)        => ZIO.succeed(Step.succeed(state))
      case _                                                => ZIO.succeed(Step.update(state))
    }
}

object LineInput {
  def run(prompt: String = "> ", initial: String = ""): RIO[TUI, String] =
    LineInput(prompt).run(initial)
}
