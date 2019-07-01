
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "0.6.1")
//addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "0.6.28")
//addSbtPlugin("org.scala-native"   % "sbt-scala-native"              % "0.3.9")

// addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.15")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
