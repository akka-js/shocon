import xerial.sbt.Sonatype._

import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val root = project.in(file(".")).aggregate(parserJS, parserJVM, facadeJS, facadeJVM).settings(sonatypeSettings)

lazy val fixResources =
  taskKey[Unit]("Fix application.conf presence on first clean build.")

lazy val parser = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "shocon-parser",
    scalacOptions ++=
      Seq("-feature", "-unchecked", "-language:implicitConversions", "-deprecation"),
    publishTo := sonatypePublishTo.value)
  .settings(sonatypeSettings)
  .settings(
    fixResources := {
      val compileConf = (resourceDirectory in Compile).value / "application.conf"
      if (compileConf.exists) {
        IO.copyFile(compileConf, (classDirectory in Compile).value / "application.conf")
      }
      val testConf = (resourceDirectory in Test).value / "application.conf"
      if (testConf.exists) {
        IO.copyFile(testConf, (classDirectory in Test).value / "application.conf")
      }
    },
    compile in Compile := (compile in Compile).dependsOn(fixResources).value,
    libraryDependencies ++= Seq(
        "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.4",
        "com.lihaoyi" %%% "fastparse" % "2.2.4",
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"))
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "1.0.0",
    parallelExecution in Test := true)

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js

lazy val facade = crossProject(JSPlatform, JVMPlatform)
  .in(file("facade"))
  .dependsOn(parser)
  .settings(
    name := "shocon",
    scalacOptions ++=
      Seq("-feature", "-unchecked", "-language:implicitConversions", "-deprecation"),
    publishTo := sonatypePublishTo.value)
  .settings(sonatypeSettings)
  .settings(
    fixResources := {
      val compileConf = (resourceDirectory in Compile).value / "application.conf"
      if (compileConf.exists) {
        IO.copyFile(compileConf, (classDirectory in Compile).value / "application.conf")
      }
      val testConf = (resourceDirectory in Test).value / "application.conf"
      if (testConf.exists) {
        IO.copyFile(testConf, (classDirectory in Test).value / "application.conf")
      }
    },
    compile in Compile := (compile in Compile).dependsOn(fixResources).value,
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
        "org.scala-lang.modules" %%% "scala-collection-compat" % "2.1.4",
        "com.lihaoyi" %%% "fastparse" % "2.2.4",
        "com.lihaoyi" %%% "utest" % "0.7.4" % "test",
        "org.scala-lang" % "scala-reflect" % scalaVersion.value % "provided"))
  .jsSettings(
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "1.0.0",
    parallelExecution in Test := true)

lazy val facadeJVM = facade.jvm
lazy val facadeJS = facade.js
