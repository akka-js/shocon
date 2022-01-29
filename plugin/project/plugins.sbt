addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.8.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

// library for plugin testing
libraryDependencies += { "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value }
