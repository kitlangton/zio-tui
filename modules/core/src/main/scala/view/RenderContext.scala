package view

class RenderContext(val textMap: TextMap, var x: Int, var y: Int) {

  def align(childSize: Size, parentSize: Size, alignment: Alignment): Unit = {
    val parentPoint = alignment.point(parentSize)
    val childPoint  = alignment.point(childSize)
    translateBy(parentPoint.x - childPoint.x, parentPoint.y - childPoint.y)
  }

  def insert(string: String, color: Color = Color.Default, style: Style = Style.Default): Unit =
    textMap.insert(string, x, y, color, style)

  def translateBy(dx: Int, dy: Int): Unit = {
    x += dx
    y += dy
  }

  def scratch(f: => Unit): Unit = {
    val x0 = x
    val y0 = y
    f
    x = x0
    y = y0
  }
}
