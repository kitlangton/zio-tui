package tui

import tui.components.Choose
import view.View
import zio._

object Main extends ZIOAppDefault {

  val chooseApp = Choose[String](View.text)

  val view =
    View.horizontal(
      View.text("Hello"),
      View.text("How are you?")
    )

  val run =
    chooseApp
      .run(Choose.State(List("cool", "how are you?", "nice")))
      .debug
      .provide(TUI.live(false))

  println(view.renderNow)
}
