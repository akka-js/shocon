
import java.{util => ju}

import org.junit.Assert._
import org.junit._

import scala.util.{Success, Failure}

import com.typesafe.config.{Config, ConfigValue, ConfigFactory}
import scala.collection.JavaConverters._

class SHoconGenericSpec {

  @Test
  def parseEmptyList() = {
    val config = ConfigFactory.parseString("""{ "a" : [] }""")
  
    assert { config != null }
    assert { config.hasPath("a") }
    
    assert { config.getStringList("a").isEmpty }
  }

  @Test
  def parseBasicValues() = {
    val config = ConfigFactory.parseString("""{ "a" : "2" }""")

    assert { config != null }

    assertEquals ( config.getString("a"), "2" )
    assertEquals ( config.getInt("a"), 2 )
  }

  @Test
  def parseLists() = {
    val config1 = ConfigFactory.parseString(
      """l =[ a
        |
        |   b
        |  c
        |
        | d ]""".stripMargin
      )

    val config2 = ConfigFactory.parseString("l = [a,b] \n[c, d]")

    assert { config1 != null && config2 != null }

    assertEquals ( config1.getStringList("l"), List("a", "b", "c", "d").asJava )
    assertEquals ( config2.getStringList("l"), config1.getStringList("l") )
  }

  @Test
  def parseNestedObjects() = {
    val config = ConfigFactory.parseString("a = { b = 1 }")

    assert { config != null }

    assertEquals ( config.getConfig("a").getInt("b"), 1 )
  }

  @Test
  def pasreNewLinesIsteadOfCommas = {
    val config = ConfigFactory.parseString("""{
    foo = 1

    bar = 2

    baz = 3}
    """)

    assert { config != null }

    assertEquals ( config.getInt("foo"), 1)
    assertEquals ( config.getInt("bar"), 2)
    assertEquals ( config.getInt("baz"), 3)
  }

  @Test
  def parseConcatenatedValues = {
    val config1 = ConfigFactory.parseString("x = {a:1, b: 2}\n {c: 3, d: 4}")

    val config2 = ConfigFactory.parseString("x = {a:1, b: 2\nc: 3, d: 4}")

    
  }
/*

  val concat_objs = "x = {a:1, b: 2}\n {c: 3, d: 4}"
  "concatenated objects" should "parse" in {

    val result = Config.parse( concat_objs  )
    result shouldBe a [ Success[_] ]

  }

  they should "parse the same as equivalent non-concat objects with same contents" in {
    Config( concat_objs  ) shouldBe Config("x = {a:1, b: 2\nc: 3, d: 4}")
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


*/
}