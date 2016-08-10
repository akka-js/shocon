import SonatypeKeys._

name := "shocon"

scalaVersion in ThisBuild := "2.11.8"

lazy val root = project.in(file(".")).
  aggregate(shoconJS, shoconJVM)

lazy val shocon = crossProject.in(file(".")).
  settings(
  	name := "shocon",
  	organization := "eu.unicredit",
  	version := "0.1.0",
    scalacOptions ++=
      Seq(
        "-feature",
        "-unchecked",
        "-language:implicitConversions"
      )
  ).
  settings(
    sonatypeSettings: _*
  ).
  settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "0.3.1",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    )
  ).
  jvmSettings(
  	libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.1.0",
    scalaJSUseRhino in Global := true,
    parallelExecution in Test := true
  )

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js

publishMavenStyle := true

pomIncludeRepository := { x => false }

credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.credentials")

pomExtra := {
  <url>https://github.com/unicredit/shocon</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:github.com/unicredit/shocon</connection>
    <developerConnection>scm:git:git@github.com:unicredit/shocon</developerConnection>
    <url>github.com/unicredit/shocon</url>
  </scm>
  <developers>
    <developer>
      <id>evacchi</id>
      <name>Edoardo Vacchi</name>
      <url>https://github.com/evacchi/</url>
    </developer>
    <developer>
      <id>andreaTP</id>
      <name>Andrea Peruffo</name>
      <url>https://github.com/andreaTP/</url>
    </developer>
  </developers>
}
