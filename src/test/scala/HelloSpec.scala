import eu.unicredit.shocon.SHocon
import org.scalatest._

import scala.util.Success

class HelloSpec extends FlatSpec with Matchers {

  val fileContents = io.Source.fromFile("src/test/resources/akka.conf").mkString


  "akka.conf" should "parse" in {
    SHocon.parse(fileContents) shouldBe a [Success[_]]
  }
}
