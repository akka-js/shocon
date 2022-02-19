addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.8.0")

addSbtPlugin(
  "org.akka-js" % "sbt-shocon" % sys.props
    .getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set")))
