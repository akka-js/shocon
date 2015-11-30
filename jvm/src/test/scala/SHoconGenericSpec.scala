import eu.unicredit.shocon.Config
import org.scalatest._

import fastparse.core.Result.Success

class SHoconGenericSpec extends FlatSpec with Matchers {


  val list = """{ "a" : [] }"""
  list should "parse" in {
    Config.parse(list) shouldBe a [Success[_]]
  }

  val list_q = """{a ="2"}"""
  list_q should "parse" in {
    val result = Config.parse(list_q)
    // println(result)
    result shouldBe a [Success[_]]
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

  val simple_arr = "l = [ a, b, c, d ]"
  val nl_arr =
    """l =[ a
      |
      |   b
      |  c
      |
      | d ]""".stripMargin
  simple_arr + nl_arr should "parse the same" in {
    Config(simple_arr) shouldBe Config(nl_arr)
  }





  val nl_in_list = "{a:[[b,c,{d:e,f:g}],[]]}"
  nl_in_list should "parse" in {
    val result = Config.parse(nl_in_list)
    // println(result)
    result shouldBe a [Success[_]]
  }

 val naked = "a { b = 1 }"
 naked should "parse" in {
   val result = Config.parse(naked)
   result shouldBe a [ Success[_] ]
 }

  val empty_nested_objs =
  """sx { "/n" {} }"""
  empty_nested_objs should "parse" in {
    val result = Config.parse(empty_nested_objs)
    result shouldBe a [ Success[_] ]
  }

  val nonempty_nested_objs =
  """sx { n { x = 2 } }"""
  nonempty_nested_objs should "parse" in {
    val result = Config.parse(nonempty_nested_objs)
    result shouldBe a [ Success[_] ]
  }

  val newlines_instead_of_commas = """{
    foo = 1

    bar = 2

    baz = 3}
  """
  newlines_instead_of_commas should "parse" in {
    val result = Config.parse( newlines_instead_of_commas  )
    result shouldBe a [ Success[_] ]
    result match {
      case Success(v,_) => v should not be Config.Object(Map())
      case _ =>
    }
  //  println (result)
  }


  val final_newline = """{
  foo = 1
  bar = 2
  baz = 3
  }
  """
  final_newline should "parse" in {
    val result = Config.parse( final_newline  )
    result shouldBe a [ Success[_] ]
  //  println (result)
  }


  val concat_bare_strings =
    """
      |x = a b c d
      |y = 10
      |""".stripMargin
  "x" should """be "a b c d"""" in {
    import eu.unicredit.shocon._
    val cfg = Config(concat_bare_strings)
    cfg("x").as[String].get shouldBe "a b c d"
  }


   val akka = io.Source.fromFile("jvm/src/test/resources/akka.conf").mkString

   "akka.conf" should "parse" in {
     val res = Config.parse(akka)
//     println(res)
     res shouldBe a [Success[_]]
   }

   val akka_long = io.Source.fromFile("jvm/src/test/resources/akka-long.conf").mkString

   "akka-long.conf" should "parse" in {
     val res = Config.parse(akka_long)
     res shouldBe a [Success[_]]
   }

}
