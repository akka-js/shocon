import SonatypeKeys._

val commonSettings = Vector(
  name := "shocon",
  organization := "org.akka-js",
  version := "0.2.0",
  scalaVersion := "2.12.2",
  crossScalaVersions  :=
    Vector("2.11.11", "2.12.2")
)

lazy val root = project.in(file(".")).
  settings(commonSettings: _*).
  aggregate(parserJS, parserJVM, facadeJS, facadeJVM)

  lazy val fixResources = taskKey[Unit](
    "Fix application.conf presence on first clean build.")

lazy val parser = crossProject.in(file(".")).
  settings(commonSettings: _*).
  settings(
    name := "shocon-parser",
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
    fixResources := {
      val compileConf = (resourceDirectory in Compile).value / "application.conf"
      if (compileConf.exists)
        IO.copyFile(
          compileConf,
          (classDirectory in Compile).value / "application.conf"
        )
      val testConf = (resourceDirectory in Test).value / "application.conf"
      if (testConf.exists) {
        IO.copyFile(
          testConf,
          (classDirectory in Test).value / "application.conf"
        )
      }
    },
    compile in Compile := (compile in Compile).dependsOn(fixResources).value,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "1.0.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    ),
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
  ).
  jvmSettings(
  	libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    scalaJSUseRhino in Global := true,
    parallelExecution in Test := true
  )

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js

lazy val facade = crossProject.in(file("facade")).
  dependsOn(parser).
  settings(commonSettings: _*).
  settings(
    name := "shocon",
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
    fixResources := {
      val compileConf = (resourceDirectory in Compile).value / "application.conf"
      if (compileConf.exists)
        IO.copyFile(
          compileConf,
          (classDirectory in Compile).value / "application.conf"
        )
      val testConf = (resourceDirectory in Test).value / "application.conf"
      if (testConf.exists) {
        IO.copyFile(
          testConf,
          (classDirectory in Test).value / "application.conf"
        )
      }
    },
    compile in Compile := (compile in Compile).dependsOn(fixResources).value,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "1.0.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    ),
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
  ).
  jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    scalaJSUseRhino in Global := true,
    parallelExecution in Test := true
  )

lazy val facadeJVM = facade.jvm
lazy val facadeJS = facade.js

lazy val plugin = project
  .settings(
    commonSettings ++
    sonatypeSettings ++
    publishingSettings: _*)
  .settings(
    name := "sbt-shocon",
    description := "sbt plugin for shocon",
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    crossScalaVersions := Seq("2.10.6"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-language:implicitConversions"),
    // configuration for testing with sbt-scripted
    ScriptedPlugin.scriptedSettings,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    publishLocal := publishLocal.dependsOn(publishLocal in facadeJS, publishLocal in facadeJVM).value
  )


lazy val publishingSettings = Seq(
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
)

publishMavenStyle in ThisBuild := true

pomIncludeRepository  in ThisBuild := { x => false }

credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.credentials")
