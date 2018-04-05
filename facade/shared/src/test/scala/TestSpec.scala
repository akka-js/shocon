import java.{util => ju}

import utest._

import scala.util.{Failure, Success}
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit

import eu.unicredit.shocon

object TestSpec extends TestSuite {

  val tests = Tests {

    'parseEmptyList - {
      val config = ConfigFactory.load()

      assert { config != null }

      assert { config.getString("loaded") == "DONE" }
    }

  }

}
