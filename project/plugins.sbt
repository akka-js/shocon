
// https://github.com/portable-scala/sbt-crossproject
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.22")
addSbtPlugin("org.portable-scala" % "sbt-crossproject"         % "0.3.1")  // (1)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.3.1")  // (2)
addSbtPlugin("org.scala-native"   % "sbt-scala-native"         % "0.3.6")  // (3)

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.5.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.3")

resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

// library for plugin testing
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
