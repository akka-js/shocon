
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
	scalaVersion := "2.11.7",
	libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
	libraryDependencies += "org.parboiled" %% "parboiled" % "2.1.0"
  ).
  jvmSettings().
  jsSettings()

lazy val shoconJVM = shocon.jvm
lazy val shoconJS = shocon.js