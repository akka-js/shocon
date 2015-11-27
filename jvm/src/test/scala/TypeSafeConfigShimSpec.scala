import com.typesafe.config.ConfigFactory
import org.scalatest._

import fastparse.core.Result.Success

class TypeSafeConfigShimSpec extends FlatSpec with Matchers {

  val cfg = ConfigFactory.parseString("""{x = 1}""")
  val x = cfg.getDuration("x")
  print("duration "+x.toNanos+"\n\n")

}
