import java.util.concurrent.TimeUnit

import com.typesafe.config.ConfigFactory
import org.scalatest._

import fastparse.core.Result.Success

class TypeSafeConfigShimSpec extends FlatSpec with Matchers {

  {
    val input =
      """ a {
        |x = 1 ms
        |}""".stripMargin

    val cfg = ConfigFactory.parseString(input)

    "a.x" should "equal 1 in milliseconds" in {
      val x = cfg.getDuration("a.x")
      x.toMillis shouldBe 1
    }

    it should "equal 1000000 in nanoseconds" in {
      val x = cfg.getDuration("a.x", TimeUnit.NANOSECONDS)
      x shouldBe 1000000
    }

  }


}
