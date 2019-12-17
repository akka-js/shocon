package tests

import java.{util => ju}

import utest._

import scala.util.{Failure, Success}
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit

import org.akkajs.shocon

object SHoconGenericSpec extends TestSuite {

  val tests = Tests {

    'parseEmptyList - {
      val config = ConfigFactory.parseString("""{ "a" : [] }""")

      assert { config != null }
      assert { config.hasPath("a") }

      assert { config.getStringList("a").isEmpty }
    }

    'parseBasicValues - {
      val config = ConfigFactory.parseString("""{ "a" : "2" }""")

      assert { config != null }

      assert { config.getString("a") == "2" }
      assert { config.getInt("a") == 2 }
    }

    'parseStringLiteralsWithSlashes - {
      val config = ConfigFactory.parseString("""a = some/path""")
      assert { config != null }
      assert { config.getString("a") == "some/path" }
    }

    'parseLists - {
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

      assert { config1.getStringList("l") == List("a", "b", "c", "d").asJava }
      assert { config2.getStringList("l") == config1.getStringList("l") }
    }

    'parseNestedObjects - {
      val config = ConfigFactory.parseString("a = { b = 1 }")

      assert { config != null }

      assert { config.getConfig("a").getInt("b") == 1 }
    }

    'pasreNewLinesIsteadOfCommas - {
      val config = ConfigFactory.parseString("""{
      foo = 1

      bar = 2

      baz = 3}
      """)

      assert { config != null }

      assert { config.getInt("foo") == 1 }
      assert { config.getInt("bar") == 2 }
      assert { config.getInt("baz") == 3 }
    }

    'parseConcatenatedValues - {
      val config1 = ConfigFactory.parseString("x = {a:1, b: 2}\n {c: 3, d: 4}")

      val config2 = ConfigFactory.parseString("x = {a:1, b: 2\nc: 3, d: 4}")

      assert { config1 != null && config2 != null }

      assert { config1 == config2 }
    }

    'parseAndConcatenateStringValues - {
      val config = ConfigFactory.parseString("""
          |x = a b c d
          |y = 10
          |""".stripMargin)

      assert { config != null }

      assert { config.getString("x") == "a b c d" }
    }

    'parseAkkaConfFiles - {
      val basic = ConfigFactory.parseString(AkkaConf.basic)
      val long = ConfigFactory.parseString(AkkaConf.long)

      assert { basic != null && long != null }

      assert { basic.getString("akka.version") == "2.0-SNAPSHOT" }
      assert { long.getString("akka.version") == "2.0-SNAPSHOT" }
    }

    'parseDurations - {
      val config = ConfigFactory.parseString(
        """ a {
          |x = 1 ms
          |}""".stripMargin
      )

      assert { config != null }

      assert { config.getDuration("a.x").toMillis.toLong == 1L }
      assert { config.getDuration("a.x", TimeUnit.NANOSECONDS).toLong == 1000000L }
    }

    'parseBytes - {
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

      assert { config.getBytes("a.b") == 9L }
      assert { config.getBytes("a.B") == 9L }
      assert { config.getBytes("a.byte") == 1L }
      assert { config.getBytes("a.bytes") == 9L }
      assert { config.getBytes("a.kB") == 9000L }
      assert { config.getBytes("a.kilobyte") == 1000L }
      assert { config.getBytes("a.kilobytes") == 9000L }
      assert { config.getBytes("a.MB") == 9000000L }
      assert { config.getBytes("a.megabyte") == 1000000L }
      assert { config.getBytes("a.megabytes") == 9000000L }
      assert { config.getBytes("a.GB") == 9000000000L }
      assert { config.getBytes("a.gigabyte") == 1000000000L }
      assert { config.getBytes("a.gigabytes") == 9000000000L }
      assert { config.getBytes("a.TB") == 9000000000000L }
      assert { config.getBytes("a.terabyte") == 1000000000000L }
      assert { config.getBytes("a.terabytes") == 9000000000000L }
      assert { config.getBytes("a.PB") == 9000000000000000L }
      assert { config.getBytes("a.petabyte") == 1000000000000000L }
      assert { config.getBytes("a.petabytes") == 9000000000000000L }
      assert { config.getBytes("a.k") == 1024L }
      assert { config.getBytes("a.K") == 1024L }
      assert { config.getBytes("a.Ki") == 1024L }
      assert { config.getBytes("a.KiB") == 1024L }
      assert { config.getBytes("a.m") == 1024L * 1024L }
      assert { config.getBytes("a.M") == 1024L * 1024L }
      assert { config.getBytes("a.Mi") == 1024L * 1024L }
      assert { config.getBytes("a.MiB") == 1024L * 1024L }
      assert { config.getBytes("a.g") == 1024L * 1024L * 1024L }
      assert { config.getBytes("a.G") == 1024L * 1024L * 1024L }
      assert { config.getBytes("a.Gi") == 1024L * 1024L * 1024L }
      assert { config.getBytes("a.GiB") == 1024L * 1024L * 1024L }

    }

    'parseBooleans - {
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

      assert { config.getBoolean("a.x1") == true }
      assert { config.getBoolean("a.x2") == true }
      assert { config.getBoolean("a.x3") == true }
      assert { config.getBoolean("a.x4") == false }
      assert { config.getBoolean("a.x5") == false }
      assert { config.getBoolean("a.x6") == false }
    }

    'parseAkkaConfiguration - {
      val config = AkkaConfig.config

      assert { config != null }

      config.getConfig("akka").entrySet()

      ()
    }

    'reloadConfigWithFallback - {
      val config1 = ConfigFactory.parseString("""{ "a" : [] }""")
      val config2 = ConfigFactory.parseString("""{ "b" : [] }""")

      assert { config1 != null && config2 != null }

      val config = config1.withFallback(config2).withFallback(config1)

      assert { config.hasPath("a") == true }
      assert { config.hasPath("b") == true }

    }

    'dottedConfigKey - {
      val configAkka =
        ConfigFactory.parseString("akka.actor.messages = on")

      assert { configAkka.hasPath("akka.actor.messages") == true }
    }

    'dottedConfigKeyWithFallback - {
      val configAkka =
        ConfigFactory.parseString("akka.actor.debug.event-stream = on").withFallback(
          ConfigFactory.parseString("""
            akka.actor.debug.event-stream = off
            akka.actor.messages = on
                                    """))

      assert { configAkka.getBoolean("akka.actor.messages") == true }
    }

    'loadDefaultConfig - {
      val config = ConfigFactory.load()

      assert { config != null }

      assert { config.getString("loaded") == "DONE" }
    }

    'unwrappedToStringInMap - {
      val config = ConfigFactory.parseString(""" a="b" """)
      val map = configToMap(config)
      assert { "b" == map("a") }
    }

    'unwrappedNumber - {
      val map = ConfigFactory.parseString(""" a=2 """).root.unwrapped
      assert { 2 == map.get("a") }
    }

    'unwrappedDuration - {
      val map = ConfigFactory.parseString(""" a=2ns """).root.unwrapped
      assert { "2ns" == map.get("a") } // Duration is not automatically unwrapped.
    }

    'unwrappedBoolean - {
      val map = ConfigFactory.parseString(""" a=true """).root.unwrapped
      assert { true == map.get("a") }
    }

    'reparseKey - {
      val key = "foo.bar.baz"
      val value = shocon.Config.StringLiteral("quux")

      val reparsed = shocon.Config.Object.reparseKey(key, value)
      val expected = shocon.Config.Object(
        Map(
          "foo" -> shocon.Config.Object(Map("bar" -> shocon.Config.Object(
            Map("baz" -> shocon.Config.StringLiteral("quux")))))))

      assert { expected == reparsed }
    }

    'mergeConfigValues - {
      val key1 = "foo.bar.baz"
      val value1 = shocon.Config.StringLiteral("quux")
      val key2 = "foo.bar.bazz"
      val value2 = shocon.Config.StringLiteral("quuxxx")

      val reparsed1 = shocon.Config.Object.reparseKey(key1, value1)
      val reparsed2 = shocon.Config.Object.reparseKey(key2, value2)

      import shocon.Config.StringLiteral
      val merged = shocon.Config.Object.mergeConfigs(reparsed1, reparsed2)
      val expected = shocon.Config.Object(
        Map(
          "foo" -> shocon.Config.Object(
            Map(
              "bar" -> shocon.Config.Object(Map(
                "baz" -> StringLiteral("quux"),
                "bazz" -> StringLiteral("quuxxx")
              ))))))

      assert { expected == merged }

    }

    'concatValues - {
      val x = ConfigFactory.parseString(
        """x="foo"
          |y=   z "bar" """.stripMargin)
      assert { "foo" == x.getString("x") }
      assert { "z bar" == x.getString("y") }
    }

    'properlyFallback - {
      val conf1 = ConfigFactory.parseString("""x = "1"""")
      val conf2 = ConfigFactory.parseString("""x = "2"""")

      val conf = conf1.withFallback(conf2)
      assert { "1" == conf.getString("x") }
    }

    'parseComments - {
      val conf = ConfigFactory.parseString(
        """
        // ignored
        x = "1"
        # ignored
        y = "foo"
        """)
      assert { "1" == conf.getString("x") }
      assert { "foo" == conf.getString("y") }
    }

    'parseListOfObjects - {
      // protect against having this parsed at compile time
      var x = ""
      val conf = ConfigFactory.parseString(
        s"""
        $x
        x = [{
            foo = 1
          }, {
            foo = 2
          }]
        """)
      val res = conf.getConfigList("x")
      assert(res.size == 2)
      assert(res.asScala(0).getInt("foo") == 1)
      assert(res.asScala(1).getInt("foo") == 2)
    }

    'parseListOfObjectsStartingNextLine - {
      // protect against having this parsed at compile time
      var x = ""
      val conf = ConfigFactory.parseString(
        s"""
        $x
        x = [
          {
            foo = 1
          }, {
            foo = 2
          }]
        """)
      val res = conf.getConfigList("x")
      assert(res.size == 2)
      assert(res.asScala(0).getInt("foo") == 1)
      assert(res.asScala(1).getInt("foo") == 2)
    }

    'parseListOfObjectsWithNewLineSeparatedObjects - {
      // protect against having this parsed at compile time
      var x = ""
      val conf = ConfigFactory.parseString(
        s"""
        $x
        x = [
          {
            foo = 1
          },
          {
            foo = 2
          }]
        """)
      val res = conf.getConfigList("x")
      assert(res.size == 2)
      assert(res.asScala(0).getInt("foo") == 1)
      assert(res.asScala(1).getInt("foo") == 2)
    }

    'parseListOfObjectsWithTrailingCommas - {
      // protect against having this parsed at compile time
      var x = ""
      val conf = ConfigFactory.parseString(
        s"""
        $x
        x = [
          {
            foo = 1
          },
          {
            foo = 2
          },
        ]
        """)
      val res = conf.getConfigList("x")
      assert(res.size == 2)
      assert(res.asScala(0).getInt("foo") == 1)
      assert(res.asScala(1).getInt("foo") == 2)
    }

    'mergeConfigObjects - {
      val conf1 = ConfigFactory.load(ConfigFactory.parseString("""
        akka.stream.materializer.initial-input-buffer-size = 2
        akka.stream.materializer.max-input-buffer-size = 2
      """))
      val conf2 = ConfigFactory.load(ConfigFactory.parseString(s"""
        akka {
          stream {
            materializer {
              creation-timeout = 20s
              initial-input-buffer-size = 4
              max-input-buffer-size = 16
              blocking-io-dispatcher = "akka.stream.default-blocking-io-dispatcher"
              dispatcher = ""
              subscription-timeout {
                mode = cancel
                timeout = 5s
              }
            }
          }
        }
        """))

      val conf = ConfigFactory.load(conf1.withFallback(conf2))
      
      assert(conf.getInt("akka.stream.materializer.initial-input-buffer-size") == 2)
      assert(conf.getConfig("akka.stream.materializer").getInt("initial-input-buffer-size") == 2)
      assert(conf.getConfig("akka.stream.materializer").getString("subscription-timeout.mode") == "cancel")
    }
  }

  private final def configToMap(config: Config): Map[String, String] = {
    import scala.collection.JavaConverters._
    config.root.unwrapped.asScala.toMap map { case (k, v) ⇒ (k → v.toString) }
  }
}
