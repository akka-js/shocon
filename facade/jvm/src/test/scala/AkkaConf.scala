object AkkaConf {

  def basic =
    io.Source.fromFile("facade/jvm/src/test/resources/akka.conf").mkString

  def long =
    io.Source.fromFile("facade/jvm/src/test/resources/akka-long.conf").mkString

}
