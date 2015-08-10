
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
	version := "0.0.1-SNAPSHOT",
	scalaVersion := "2.11.7"
  ).
  jvmSettings(
  	libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.0",
  	libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ).
  jsSettings(
  	libraryDependencies += "org.parboiled" %%% "parboiled" % "2.1.1-SNAPSHOT"
  )

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js