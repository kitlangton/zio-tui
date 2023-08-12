package tui.view

import java.io.OutputStream

class EscapeCodes(out: OutputStream) {
  private def CSI(sequence: String): Unit =
    out.write(("\u001b[" + sequence).getBytes)

  private def CSI(n: Int, command: Char): Unit = CSI(s"$n$command")

  private def CSI(mode: Char, n: Int, command: Char): Unit =
    CSI(s"$mode$n;$command")

  def hideCursor(): Unit      = CSI('?', 25, 'l')
  def showCursor(): Unit      = CSI('?', 25, 'h')
  def clear(): Unit           = CSI(2, 'J')
  def alternateBuffer(): Unit = CSI('?', 47, 'h')
  def normalBuffer(): Unit    = CSI('?', 47, 'l')
}
