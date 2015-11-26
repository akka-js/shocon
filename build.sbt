name := "shocon"

lazy val root = project.in(file(".")).
  aggregate(shoconJS, shoconJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

lazy val shocon = crossProject.in(file(".")).
  settings(
	name := "shocon",
	organization := "eu.unicredit",
	version := "0.0.2-SNAPSHOT",
	scalaVersion := "2.11.7"
  ).
  settings(
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "0.3.1"
  ).
  jvmSettings(
  	libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ).
  jsSettings(
  )

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js
