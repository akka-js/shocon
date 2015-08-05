import eu.unicredit.shocon.SHocon
import org.parboiled2.ParseError
import org.scalatest._

import scala.util.{Failure, Success}

class HelloSpec extends FlatSpec with Matchers {


  // val list = "{ a = [] }"
  // list should "parse" in {
  //   SHocon.parse(list) shouldBe a [Success[_]]
  // }


  val list2 = "{a=1}"
  list2 should "parse" in {
    SHocon.parse(list2) shouldBe a [Success[_]]
  }

  val list_q = "{a =\"2\"}"
  list_q should "parse" in {
    SHocon.parse(list_q) shouldBe a [Success[_]]
  }


  val list_no_nl = "{ a  = [] }"
  list_no_nl should "parse" in {
    SHocon.parse(list_no_nl) shouldBe a [Success[_]]
  }



  val list_nl = "{a = [] \n }"
  list_nl should "parse" in {
    SHocon.parse(list_nl) shouldBe a [Success[_]]
  }


  val nl_in_empty_list = "{a = [  \n \n ] \n }"
  nl_in_empty_list should "parse" in {
    SHocon.parse(nl_in_empty_list) shouldBe a [Success[_]]
  }


  val nl_in_list = "{a:[[b,c,{d:e,f:g}],[]]}"
  nl_in_list should "parse" in {
    SHocon.parse(nl_in_list) shouldBe a [Success[_]]
  }

 val fileContents = io.Source.fromFile("src/test/resources/akka.conf").mkString

 "akka.conf" should "parse" in {
   val res = SHocon.parse(fileContents)
//    res match {
//      case Failure(ParseError(p1,p2,traces)) => print(p1,p2,traces)
//      case _=>
//    }
   res shouldBe a [Success[_]]
 }
}
