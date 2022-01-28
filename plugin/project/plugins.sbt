addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")

resolvers += Resolver.typesafeIvyRepo("releases")

resolvers += "Typesafe repository".at("http://repo.typesafe.com/typesafe/releases/")

resolvers += "sonatype-releases".at("https://oss.sonatype.org/content/repositories/releases/")

// library for plugin testing
libraryDependencies += { "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value }
