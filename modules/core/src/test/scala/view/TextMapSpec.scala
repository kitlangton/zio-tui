package view

import tui.components.Choose
import zio.test._
import zio._

object TextMapSpec extends ZIOSpecDefault {

  final case class Food(name: String, price: Int)

  val foods: List[Food] = List(
    Food("a", 1),
    Food("b", 2),
    Food("c", 3),
    Food("d", 4),
    Food("5", 5)
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
        val tm  = foodMap(foods)
        val tm2 = foodMap(foods ++ foods.map(_.copy(price = 6)))
//        val tm  = foodMap(foods ++ foods)
//        val tm2 = foodMap(foods)

        println(TextMap.moveCursor(0, 1000))
        println(tm.toString)
        Thread.sleep(1000)
//        println(TextMap.moveUp(20))
        diff(tm, tm2)
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
    println(diff.toList)
  }
}
