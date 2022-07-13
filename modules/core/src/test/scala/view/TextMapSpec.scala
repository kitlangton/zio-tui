package view

import tui.components.Choose
import zio.test.*
import zio.*

object TextMapSpec extends ZIOSpecDefault {

  final case class Food(name: String, price: Int)

  val foods: List[Food] = List(
    Food("AA AA " * 3, 1),
    Food("BB BB " * 3, 2),
    Food("CC CC " * 3, 3),
    Food("DD DD " * 3, 4),
    Food("EE EE " * 3, 5)
  )

  val foods2: List[Food] = List(
    Food("a a a " * 2, 1),
    Food("a a a " * 3, 2),
    Food("bB BB " * 1, 2),
    Food("CC CC " * 3, 3),
    Food("d d d " * 2, 4),
    Food("eE EE " * 3, 5)
  )

  def renderFood(food: Food): View =
    View.horizontal(
      View.text(food.name).blue.bold,
      View.text(s"$$${food.price}").green
    )

  def foodMap(foods: List[Food]): TextMap =
    Choose[Food](renderFood).render(Choose.State(foods)).renderTextMap

  def spec =
    suite("TextMapSpec")(
      test("TextMap") {
        val tm  = foodMap(foods2)
        val tm2 = foodMap(foods)

        println(TextMap.moveCursor(0, 1000))
        println(tm.toString)
        Thread.sleep(1000)
//        println(TextMap.moveUp(20))
        diff(tm, tm2)
        Thread.sleep(1000)
        diff(tm2, tm)
        Thread.sleep(1000)
//        diff(tm2, tm)
//        Thread.sleep(1000)
//        diff(tm, tm2)
//        Thread.sleep(1000)
        assertTrue(true)
      }
    )

  def diff(tm1: TextMap, tm2: TextMap): Unit = {
    val diff = TextMap.diff(tm1, tm2)
    println(diff)
//    println(diff.toList)
  }
}
