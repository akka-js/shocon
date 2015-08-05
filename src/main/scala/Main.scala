import eu.unicredit.shocon.ConfigParser
import org.parboiled2._

object Main {
  def main(args: Array[String]): Unit = {

    print(new ConfigParser(
      """
        |foobar = [
        |
        |
        |   1
        |   2,
        |
        |  {
        |      x =   10
        |
        |      y = 123,
        |
        |
        |
        |      z = 23232
        |      }
        |
        |]
      """.stripMargin.trim()).InputLine.run()) // evaluates to `scala.util.Success(2)`
  }
}
