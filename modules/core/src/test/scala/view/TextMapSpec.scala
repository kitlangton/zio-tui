package view

import zio.test._
import zio._

object TextMapSpec extends ZIOSpecDefault {
  def spec =
    suite("TextMapSpec")(
      test("TextMap") {
        val tm = TextMap.ofDim(8, 4, ".")
        tm.setColor(3, 3, Color.Red)

        val tm2 =
          TextMap
            .ofDim(5, 5, "-")
        tm2.setColor(4, 3, Color.Red)

        val diff = TextMap.diff(tm, tm2)
//        print(TextMap.moveCursor(0, 0))
        println(tm.toString)
        println(diff)
        val diff2 = TextMap.diff(tm2, tm)
        //        print(TextMap.moveCursor(0, 0))
        println(diff2)
        println(diff.toList)
//        println(tm2.toString)
        assertTrue(true)
      }
    )
}
