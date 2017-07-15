
lazy val root = crossProject.in(file("."))
  .dependsOn(lib)
  .enablePlugins(ShoconJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    name := "basic",
    version := "0.1.0-SNAPSHOT",
    description := "Basic test for the shocon sbt plugin"
  )


lazy val rootJVM = root.jvm
lazy val rootJS = root.js

lazy val lib = crossProject.in(file("lib"))
  .enablePlugins(ShoconJSPlugin)
  .settings(
    scalaVersion := "2.12.2",
    name := "lib"
  )

lazy val libJVM = lib.jvm
lazy val libJS = lib.js
