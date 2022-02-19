package tests

import scala.util.Using

object AkkaConf {

  def basic: String = Using.resource(io.Source.fromFile("facade/jvm/src/test/resources/akka.conf")) { _.mkString }

  def long: String = Using.resource(io.Source.fromFile("facade/jvm/src/test/resources/akka-long.conf")) { _.mkString }

}
