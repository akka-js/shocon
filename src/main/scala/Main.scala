import eu.unicredit.shocon.ConfigParser
import org.parboiled2._

object Main {
  def main(args: Array[String]): Unit = {

    print(new ConfigParser(
      """
        |{a:1,b:2,c:3}
      """.stripMargin.trim()).InputLine.run()) // evaluates to `scala.util.Success(2)`
  }
}
