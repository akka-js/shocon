addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.16")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
