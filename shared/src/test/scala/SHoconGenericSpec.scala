/* Copyright 2016 UniCredit S.p.A.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import java.{util => ju}

import org.junit.Assert._
import org.junit._

import scala.util.{Success, Failure}

import com.typesafe.config.{Config, ConfigValue, ConfigFactory}
import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit

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

    assert { config1 != null && config2 != null }

    assertEquals ( config1, config2)
  }

  @Test
  def parseAndConcatenateStringValues = {
    val config = ConfigFactory.parseString(
      """
        |x = a b c d
        |y = 10
        |""".stripMargin)

    assert { config != null }

    assertEquals(config.getString("x"), "a b c d")
  }

  @Test
  def parseAkkaConfFiles = {
    val basic = ConfigFactory.parseString(AkkaConf.basic)
    val long = ConfigFactory.parseString(AkkaConf.long)

    assert { basic != null && long != null }

    assertEquals (basic.getString("akka.version"), "2.0-SNAPSHOT")
    assertEquals (long.getString("akka.version"), "2.0-SNAPSHOT")
  }

  @Test
  def parseDurations = {
    val config = ConfigFactory.parseString(
      """ a {
        |x = 1 ms
        |}""".stripMargin
      )

    assert { config != null }

    assertEquals (config.getDuration("a.x").toMillis.toLong, 1L)
    assertEquals (config.getDuration("a.x", TimeUnit.NANOSECONDS).toLong, 1000000L)
  }

  @Test
  def parseBooleans = {
    val config = ConfigFactory.parseString(
      """ a {
        |x1 = true
        |x2 = on
        |x3 = yes
        |x4 = false
        |x5 = off
        |x6 = no
        |}""".stripMargin
      )

    assert { config != null }

    assertEquals (config.getBoolean("a.x1"), true)
    assertEquals (config.getBoolean("a.x2"), true)
    assertEquals (config.getBoolean("a.x3"), true)
    assertEquals (config.getBoolean("a.x4"), false)
    assertEquals (config.getBoolean("a.x5"), false)
    assertEquals (config.getBoolean("a.x6"), false)
  }

  @Test
  def parseAkkaConfiguration = {
    val config = AkkaConfig.config

    assert { config != null }
  }
}
