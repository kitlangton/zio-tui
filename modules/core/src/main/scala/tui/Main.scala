package tui

import tui.components.Choose
import tui.view.View
import zio._

object Main extends App {

  val chooseApp = Choose[String](View.text)

  val deps = Chunk(
    Chunk("zio", "zio-streams", "zio-test"),
    Chunk("caliban", "california"),
    Chunk("nice")
  )

  def depView(deps: Chunk[String]) =
    View.vertical(
      deps.map { dep =>
        View.text(dep.padTo(20, ' ')).cyan
      } *
    )

  val view =
    View.vertical(
      View.vertical("hello", "there", "babies"),
      "gents",
      "nice"
    )

  val run =
    Unsafe.unsafe { implicit unsafe: Unsafe =>
      zio.Runtime.default.unsafe.run {
        chooseApp
          .run(Choose.State(List("zio!-longer", "zio-streams-big", "zio-test", "nice")))
          .provide(TUI.live(false))
      }
    }
}
