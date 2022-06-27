package tui

import tui.components.Choose
import view.{Alignment, Color, VerticalAlignment, View}
import zio.Chunk

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

  private val dependencies = deps.map(depView)

  val view =
    View.vertical(
      View.vertical("hello", "there", "babies"),
      "gents",
      "nice"
    )

  println(view.renderNow)
}
