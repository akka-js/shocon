addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.8.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

resolvers += "sonatype-releases".at("https://oss.sonatype.org/content/repositories/releases/")
