import com.typesafe.config.ConfigFactory
import org.scalatest._

import fastparse.core.Result.Success

class TypeSafeConfigShimSpec extends FlatSpec with Matchers {

  {
    val input = """x = "1 ms" """

    input should "have x == 1 ms" in {
      val cfg = ConfigFactory.parseString(input)
      val x = cfg.getDuration("x")
      x.toMillis shouldBe 1
    }

  }


}
