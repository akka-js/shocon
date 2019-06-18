import xerial.sbt.Sonatype._

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

// import scala.scalanative.sbtplugin.ScalaNativePluginInternal.NativeTest

lazy val root = project
  .in(file("."))
  // .aggregate(parserJS, parserJVM, parserNative, facadeJS, facadeJVM, facadeNative)
  .aggregate(parserJS, parserJVM, facadeJS, facadeJVM)
  .settings(sonatypeSettings)

lazy val fixResources =
  taskKey[Unit]("Fix application.conf presence on first clean build.")

// lazy val parser = crossProject(JSPlatform, JVMPlatform, NativePlatform)
lazy val parser = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "shocon-parser",
    scalacOptions ++=
      Seq(
        "-feature",
        "-unchecked",
        "-language:implicitConversions"
      ),
    publishTo := sonatypePublishTo.value
  )
  .settings(sonatypeSettings)
  .settings(
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
      "com.lihaoyi" %%% "fastparse" % "2.1.2",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    )
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    parallelExecution in Test := true
  )
  // .nativeSettings(
  //   resolvers += Resolver.sonatypeRepo("releases"),
  //   nativeLinkStubs := true,
  //   libraryDependencies += "org.akka-js" %%% "scalanative-java-time" % "0.0.2",
  //   // disable Native testing with Scala 2.12
  //   (test in Test) := (Def.taskDyn {
  //     if (scalaVersion.value.startsWith("2.11"))
  //       (test in NativeTest)
  //     else Def.task { }
  //   }).value
  // )

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js
// lazy val parserNative = parser.native

// lazy val facade = crossProject(JSPlatform, JVMPlatform, NativePlatform)
lazy val facade = crossProject(JSPlatform, JVMPlatform)
  .in(file("facade"))
  .dependsOn(parser)
  .settings(
    name := "shocon",
    scalacOptions ++=
      Seq(
        "-feature",
        "-unchecked",
        "-language:implicitConversions"
      ),
    publishTo := sonatypePublishTo.value
  )
  .settings(sonatypeSettings)
  .settings(
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
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "1.0.0",
      "com.lihaoyi" %%% "utest" % "0.6.3" % "test",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"
    )
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0",
    parallelExecution in Test := true
  )
  // .nativeSettings(
  //   resolvers += Resolver.sonatypeRepo("releases"),
  //   nativeLinkStubs := true,
  //   libraryDependencies += "org.akka-js" %%% "scalanative-java-time" % "0.0.1",
  //   // disable Native testing with Scala 2.12
  //   (test in Test) := (Def.taskDyn {
  //     if (scalaVersion.value.startsWith("2.11"))
  //       (test in NativeTest)
  //     else Def.task { }
  //   }).value
  // )

lazy val facadeJVM = facade.jvm
lazy val facadeJS = facade.js
// lazy val facadeNative = facade.native
