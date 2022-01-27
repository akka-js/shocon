addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")

// addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.15")

resolvers += "sonatype-releases".at("https://oss.sonatype.org/content/repositories/releases/")
