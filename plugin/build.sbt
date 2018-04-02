import xerial.sbt.Sonatype._

lazy val plugin = project.in(file("."))
  .settings(sonatypeSettings)
  .enablePlugins(ScriptedPlugin)
  .settings(
    name := "sbt-shocon",
    description := "sbt plugin for shocon",
    sbtPlugin := true,
    scalaVersion := "2.12.4",
    crossSbtVersions := Vector("0.13.17", "1.1.2"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion),
    scalacOptions ++= Seq("-feature",
                          "-unchecked",
                          "-language:implicitConversions"),
    // configuration for testing with sbt-scripted
    scriptedLaunchOpts ++= Seq("-Xmx1024M",
                               "-Dplugin.version=" + version.value),
    scriptedBufferLog := false
  )
