package view

import scala.collection.mutable

class TextMap(
  text: Array[Array[String]],
  colors: Array[Array[Color]],
  styles: Array[Array[Style]],
  val width: Int,
  val height: Int
) { self =>

  def apply(x: Int, y: Int): String =
    if (0 <= x && x < width && 0 <= y && y < height)
      text(y)(x)
    else " "

  def setColor(x: Int, y: Int, color: Color): Unit =
    if (0 <= x && x < width && 0 <= y && y < height)
      colors(y)(x) = color

  def setStyle(x: Int, y: Int, style: Style): Unit =
    if (0 <= x && x < width && 0 <= y && y < height)
      styles(y)(x) = style

  def getColor(x: Int, y: Int): Color =
    if (0 <= x && x < width && 0 <= y && y < height)
      colors(y)(x)
    else Color.Default

  def getStyle(x: Int, y: Int): Style =
    if (0 <= x && x < width && 0 <= y && y < height)
      styles(y)(x)
    else Style.Default

  def update(x: Int, y: Int, string: String): Unit =
    if (0 <= x && x < width && 0 <= y && y < height)
      text(y)(x) = string

  def add(char: Char, x: Int, y: Int, color: Color = Color.Default, style: Style = Style.Default): Unit = {
    self(x, y) = char.toString
    setColor(x, y, color)
    setStyle(x, y, style)
  }

  def insert(string: String, x: Int, y: Int, color: Color = Color.Default, style: Style = Style.Default): Unit = {
    var currentX = x
    string.foreach { char =>
      add(char, currentX, y, color, style)
      currentX += 1
    }
  }

  override def toString: String = {
    val builder      = new StringBuilder()
    var color: Color = Color.Default
    var style: Style = Style.Default
    var y            = 0
    text.foreach { line =>
      var x = 0
      line.foreach { char =>
        val newColor = colors(y)(x)
        val newStyle = styles(y)(x)

        // TODO: Clean up this nonsense. Actually model the terminal styling domain.
        if (newColor != color || newStyle != style) {
          color = newColor
          style = newStyle
          builder.addAll(scala.Console.RESET)
          builder.addAll(color.code)
          builder.addAll(style.code)
        }
        builder.addAll(char)
        x += 1
      }
      if (y < height - 1) {
        y += 1
        builder.addOne('\n')
      }
    }
    builder.toString()
  }
}

object TextMap {
  def ofDim(width: Int, height: Int, empty: String = " "): TextMap =
    new TextMap(
      Array.fill(height, width)(empty),
      Array.fill(height, width)(Color.Default),
      Array.fill(height, width)(Style.Default),
      width,
      height
    )

  lazy val ec = new EscapeCodes(java.lang.System.out)

  def diff(oldMap: TextMap, newMap: TextMap): String = {
    val result = new mutable.StringBuilder()

    val height = newMap.height
    val width  = newMap.width max oldMap.width

    result.addAll(moveUp(oldMap.height))
    result.addAll(moveLeft(oldMap.width))

    var lastEditX = 0
    var lastEditY = 0
    var x         = 0
    var y         = 0

    def move(toX: Int, toY: Int): Unit = {
      val dx = toX - lastEditX

      if (dx > 0)
        result.addAll(moveRight(dx))
      else if (dx < 0)
        result.addAll(moveLeft(-dx))

      val dy = toY - lastEditY

      if (dy > 0)
        result.addAll(moveDown(dy))
      else if (dy < 0)
        result.addAll(moveUp(-dy))

      lastEditX = x + 1
      lastEditY = y
    }

    while (y < height) {
      while (x < width) {
        // If at the end of the screen, moving down doesn't work.
        if (x == width - 1 && y >= oldMap.height) {
          move(x, y)
          result.addAll("\n")
          lastEditY += 1
          lastEditX = 0
        }

        val oldChar = oldMap(x, y)

        val newChar  = newMap(x, y)
        val oldColor = oldMap.getColor(x, y)
        val newColor = newMap.getColor(x, y)
        val oldStyle = oldMap.getStyle(x, y)
        val newStyle = newMap.getStyle(x, y)

        if (oldChar != newChar || oldColor != newColor || oldStyle != newStyle) {
          move(x, y)
          result.addAll(newColor.code)
          result.addAll(newStyle.code)
          result.addAll(newChar)
          result.addAll(scala.Console.RESET)
        }
        x += 1
      }
      x = 0
      y += 1
    }

    move(width, newMap.height - 1)

    result.toString()
  }

  def moveCursor(x: Int, y: Int): String = s"\u001b[${y + 1};${x + 1}H"
  def moveDown(n: Int = 1): String       = s"\u001b[${n}B"
  def moveRight(n: Int = 1): String      = s"\u001b[${n}C"
  def moveUp(n: Int = 1): String         = s"\u001b[${n}A"
  def moveLeft(n: Int = 1): String       = s"\u001b[${n}D"

}
