package tui.components

import view.View.string2View
import view.{KeyEvent, View}
import tui.{TUI, TerminalApp, TerminalEvent}
import tui.TerminalApp.Step
import zio.RIO

case class LineInput(prompt: String) extends TerminalApp[Any, String, String] {

  override def render(state: String): View =
    View.horizontal(0)(s"$prompt".bold.green, state.bold, View.text(" ").reversed.blinking)

  override def update(state: String, event: TerminalEvent[Any]): Step[String, String] =
    event match {
      case TerminalEvent.SystemEvent(KeyEvent.Character(c)) => Step.update(state + c)
      case TerminalEvent.SystemEvent(KeyEvent.Delete)       => Step.update(state.dropRight(1))
      case TerminalEvent.SystemEvent(KeyEvent.Exit)         => Step.exit
      case TerminalEvent.SystemEvent(KeyEvent.Enter)        => Step.succeed(state)
      case _                                                => Step.update(state)
    }
}

object LineInput {
  def run(prompt: String = "> ", initial: String = ""): RIO[TUI, String] =
    LineInput(prompt).run(initial)
}
