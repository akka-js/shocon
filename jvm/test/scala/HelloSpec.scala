import eu.unicredit.shocon.Config
import org.scalatest._

import scala.util.{Failure, Success}

class HelloSpec extends FlatSpec with Matchers {


  // val list = "{ a = [] }"
  // list should "parse" in {
  //   SHocon.parse(list) shouldBe a [Success[_]]
  // }


  val list2 = "{a=1}"
  list2 should "parse" in {
    Config.parse(list2) shouldBe a [Success[_]]
  }

  val list_q = "{a =\"2\"}"
  list_q should "parse" in {
    Config.parse(list_q) shouldBe a [Success[_]]
  }


  val list_no_nl = "{ a  = [] }"
  list_no_nl should "parse" in {
    Config.parse(list_no_nl) shouldBe a [Success[_]]
  }



  val list_nl = "{a = [] \n }"
  list_nl should "parse" in {
    Config.parse(list_nl) shouldBe a [Success[_]]
  }


  val nl_in_empty_list = "{a = [  \n \n ] \n }"
  nl_in_empty_list should "parse" in {
    Config.parse(nl_in_empty_list) shouldBe a [Success[_]]
  }


  val nl_in_list = "{a:[[b,c,{d:e,f:g}],[]]}"
  nl_in_list should "parse" in {
    Config.parse(nl_in_list) shouldBe a [Success[_]]
  }

 val akka = io.Source.fromFile("src/test/resources/akka.conf").mkString

 "akka.conf" should "parse" in {
   val res = Config.parse(akka)
   res shouldBe a [Success[_]]
 }

 val akka_long = io.Source.fromFile("src/test/resources/akka-long.conf").mkString

 "akka-long.conf" should "parse" in {
   val res = Config.parse(akka_long)
   res shouldBe a [Success[_]]
 }

}
