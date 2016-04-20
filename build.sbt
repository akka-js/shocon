name := "shocon"

lazy val root = project.in(file(".")).
  aggregate(shoconJS, shoconJVM)

lazy val shocon = crossProject.in(file(".")).
  settings(
  	name := "shocon",
  	organization := "eu.unicredit",
  	version := "0.0.2-SNAPSHOT",
  	scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-language:implicitConversions")
  ).
  settings(
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "0.3.1"
  ).
  jvmSettings(
  	libraryDependencies += "com.novocode" % "junit-interface" % "0.9" % "test"
  ).
  jsConfigure(
    _.enablePlugins(ScalaJSJUnitPlugin)
  ).
  jsSettings(
    scalaJSUseRhino in Global := true,
    parallelExecution in Test := true
  )

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js
