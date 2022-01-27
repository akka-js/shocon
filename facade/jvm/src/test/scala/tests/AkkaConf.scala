package tests

object AkkaConf {

  def basic: String = {
    val bufferedSource = io.Source.fromFile("facade/jvm/src/test/resources/akka.conf")
    try {
      bufferedSource.mkString
    } finally {
      bufferedSource.close()
    }
  }

  def long: String = {
    val bufferedSource = io.Source.fromFile("facade/jvm/src/test/resources/akka-long.conf")
    try {
      bufferedSource.mkString
    } finally {
      bufferedSource.close()
    }
  }

}
