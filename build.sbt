
// https://github.com/portable-scala/sbt-crossproject
// (5) shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

import SonatypeKeys._

val commonSettings = Vector(
  name := "shocon",
  organization := "org.akka-js",
  version := "0.2.2",
  scalaVersion := "2.11.12",
  crossScalaVersions  :=
    Vector("2.11.12", "2.12.4")
)

val nativeSettings = Seq(
    nativeGC := "boehm",
    nativeMode := "debug",
    nativeLinkStubs := false,
    crossScalaVersions := Seq("2.11.12") // no 2.12.X
)

lazy val root = project.in(file(".")).
  settings(commonSettings: _*).
  aggregate(
    parserJVM, parserJS, parserNative, 
    facadeJVM, facadeJS, facadeNative 
  )

  lazy val fixResources = taskKey[Unit](
    "Fix application.conf presence on first clean build.")

lazy val parser = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file(".")).
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
  ).
  nativeSettings(
    nativeSettings
  )

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js
lazy val parserNative = parser.native

lazy val facade = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file("facade")).
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
  ).
  nativeSettings(
    nativeSettings
  )

lazy val facadeJVM = facade.jvm
lazy val facadeJS = facade.js
lazy val facadeNative = facade.native

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
    // FIXME: see .travis.yml plugin/scripted
    // sbtBinaryVersion in update := (sbtBinaryVersion in pluginCrossBuild).value,
    // addSbtPlugin("org.portable-scala" % "sbt-platform-deps" % "1.0.0-M2"),
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-language:implicitConversions"),
    // configuration for testing with sbt-scripted
    ScriptedPlugin.scriptedSettings,
    scriptedLaunchOpts ++= Seq("-Xmx1024M", "-Dplugin.version=" + version.value),
    scriptedBufferLog := false,
    publishLocal := publishLocal.dependsOn(
        publishLocal in parserJVM,
        publishLocal in parserJS, 
        publishLocal in parserNative,
        publishLocal in facadeJVM,
        publishLocal in facadeJS, 
        publishLocal in facadeNative
    ).value
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
