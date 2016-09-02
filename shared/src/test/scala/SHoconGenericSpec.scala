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
  def parseBytes = {
    val config = ConfigFactory.parseString(
      """ a {
        |b = 9 b
        |B = 9 b
        |byte = 1 byte
        |bytes = 9 bytes
        |kB = 9 kB
        |kilobyte = 1 kilobyte
        |kilobytes = 9 kilobyte
        |MB = 9 MB
        |megabyte = 1 megabyte
        |megabytes = 9 megabytes
        |GB = 9 GB
        |gigabyte = 1 gigabyte
        |gigabytes = 9 gigabytes
        |TB = 9 TB
        |terabyte = 1 terabyte
        |terabytes = 9 terabytes
        |PB = 9 PB
        |petabyte = 1 petabyte
        |petabytes = 9 petabytes
        |EB = 9 EB
        |K = 1 K
        |k = 1 k
        |Ki = 1 Ki
        |KiB = 1 KiB
        |m = 1 m
        |M = 1 M
        |Mi = 1 Mi
        |MiB = 1 MiB
        |g = 1 g
        |G = 1 G
        |Gi = 1 Gi
        |GiB = 1 GiB
        |}""".stripMargin
    )

    assert { config != null }

    assertEquals (config.getBytes("a.b"),                             9L)
    assertEquals (config.getBytes("a.B"),                             9L)
    assertEquals (config.getBytes("a.byte"),                          1L)
    assertEquals (config.getBytes("a.bytes"),                         9L)
    assertEquals (config.getBytes("a.kB"),                         9000L)
    assertEquals (config.getBytes("a.kilobyte"),                   1000L)
    assertEquals (config.getBytes("a.kilobytes"),                  9000L)
    assertEquals (config.getBytes("a.MB"),                      9000000L)
    assertEquals (config.getBytes("a.megabyte"),                1000000L)
    assertEquals (config.getBytes("a.megabytes"),               9000000L)
    assertEquals (config.getBytes("a.GB"),                   9000000000L)
    assertEquals (config.getBytes("a.gigabyte"),             1000000000L)
    assertEquals (config.getBytes("a.gigabytes"),            9000000000L)
    assertEquals (config.getBytes("a.TB"),                9000000000000L)
    assertEquals (config.getBytes("a.terabyte"),          1000000000000L)
    assertEquals (config.getBytes("a.terabytes"),         9000000000000L)
    assertEquals (config.getBytes("a.PB"),             9000000000000000L)
    assertEquals (config.getBytes("a.petabyte"),       1000000000000000L)
    assertEquals (config.getBytes("a.petabytes"),      9000000000000000L)
    assertEquals (config.getBytes("a.k"),                          1024L)
    assertEquals (config.getBytes("a.K"),                          1024L)
    assertEquals (config.getBytes("a.Ki"),                         1024L)
    assertEquals (config.getBytes("a.KiB"),                        1024L)
    assertEquals (config.getBytes("a.m"),                    1024L*1024L)
    assertEquals (config.getBytes("a.M"),                    1024L*1024L)
    assertEquals (config.getBytes("a.Mi"),                   1024L*1024L)
    assertEquals (config.getBytes("a.MiB"),                  1024L*1024L)
    assertEquals (config.getBytes("a.g"),              1024L*1024L*1024L)
    assertEquals (config.getBytes("a.G"),              1024L*1024L*1024L)
    assertEquals (config.getBytes("a.Gi"),             1024L*1024L*1024L)
    assertEquals (config.getBytes("a.GiB"),            1024L*1024L*1024L)

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

    config.getConfig("akka").entrySet()

    ()
  }

  @Test
  def reloadConfigWithFallback() = {
    val config1 = ConfigFactory.parseString("""{ "a" : [] }""")
    val config2 = ConfigFactory.parseString("""{ "b" : [] }""")

    assert ( config1 != null && config2 != null , "both config were null" )

    val config = config1.withFallback(config2).withFallback(config1)

    assert ( config.hasPath("a") , "config must have path a" )
    assert ( config.hasPath("b") , "config must have path b" )


  }

  @Test
  def dottedConfig() = {
    val configAkka =
      ConfigFactory.parseString("akka.actor.messages = on")

//    val fallback =
//      ConfigFactory.parseString("""
//          akka.actor.debug.event-stream = off
//          akka.actor.messages = on
//                                """)


    println(configAkka)
    assert (configAkka.hasPath("akka.actor.messages"), "config must have path akka.actor.messages")

  }

  @Test
  def loadDefaultConfig() = {
    val config = ConfigFactory.load()

    assert { config != null }

    assert { config.getString("loaded") == "DONE" }
  }

  @Test
  def unwrappedToStringInMap() = {
    val config = ConfigFactory.parseString(""" a="b" """)
    val map = configToMap(config)
    assertEquals("b", map("a"))
  }




  private final def configToMap(config: Config): Map[String, String] = {
    import scala.collection.JavaConverters._
    config.root.unwrapped.asScala.toMap map { case (k, v) ⇒ (k → v.toString) }
  }
}
