package view

import zio.test._

object TextMapSpec extends ZIOSpecDefault {
  def spec =
    suite("TextMapSpec")(
      test("TextMap") {
        val tm = TextMap.ofDim(8, 8, ".")

        val tm2 =
          TextMap
            .ofDim(5, 3, "-")

        val diff = TextMap.diff(tm, tm2)
//        print(TextMap.moveCursor(0, 0))
        println(tm.toString)
        println(diff)
        println(diff.toList)
//        println(tm2.toString)
        assertTrue(true)
      }
    )
}
