package examples

import zio._
import tui._
import tui.components.{Choose, LineInput}
import view._

object Simple extends ZIOAppDefault {

  final case class Food(name: String, price: Int)

  val foods = List(
    Food("Pizza", 10),
    Food("Burger", 5),
    Food("Coffee", 2),
    Food("Tea", 1)
  )

  def renderFood(food: Food): View =
    View.horizontal(
      View.text(food.name).blue.bold,
      View.text(s"$$${food.price}").green
    )

  override val run = {
    for {
      name     <- LineInput.run("What's your name? ")
      selected <- Choose.run(foods)(renderFood)
      _        <- ZIO.debug(s"$name selected: ${selected}")
    } yield ()
  }.provide(
    TUI.live(false)
  )
}
