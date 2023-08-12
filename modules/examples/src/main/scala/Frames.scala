import tui.view.View

object FrameExamples {
  def main(args: Array[String]): Unit = {
    println(
      View
        .horizontal(
          View
            .text("zio-app")
            .center
            .bordered
            .yellow
            .underlined,
          View
            .text("zio-app")
            .center
            .bordered
            .reversed
            .red
        )
        .padding(bottom = 1)
//        .renderNow
        .render(42, 7)
    )

    println(
      View
        .horizontal(
          View
            .text("zio-app")
            .center
            .bordered
            .yellow
            .underlined,
          View
            .text("zio-app")
            .center
            .bordered
            .reversed
            .red
        )
        .padding(bottom = 2)
//        .renderNow
        .render(42, 7)
    )
  }

}
