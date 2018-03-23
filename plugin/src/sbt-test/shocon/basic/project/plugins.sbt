
// https://github.com/portable-scala/sbt-crossproject
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.22")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "0.3.1")  // (1)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.1")  // (2)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"         % "0.3.6")  // (3)

addSbtPlugin("org.akka-js" % "sbt-shocon" % sys.props.getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set")))
