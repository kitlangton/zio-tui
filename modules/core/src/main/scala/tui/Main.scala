package tui

import tui.components.Choose
import view.{Alignment, Color, VerticalAlignment, View}
import zio.{Chunk, Unsafe, ZIOAppDefault}

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
      }: _*
    )

  val view =
    View.vertical(
      View.vertical("hello", "there", "babies"),
      "gents",
      "nice"
    )

  val run =
    Unsafe.unsafe { implicit unsafe =>
      zio.Runtime.default.unsafe.run {
        {
          for {
            _ <- chooseApp
                   .run(Choose.State(List("zio!-longer", "zio-streams-big", "zio-test", "nice")))
            _ <- chooseApp
                   .run(Choose.State(List("zio!", "zio-streams")))
          } yield ()
        }
          .provide(TUI.live(false))
      }
    }
}
