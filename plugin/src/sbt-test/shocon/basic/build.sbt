// https://github.com/portable-scala/sbt-crossproject
// (5) shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

lazy val root = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file("."))
  .dependsOn(lib)
  .enablePlugins(ShoconPlugin)
  .settings(
    scalaVersion := "2.11.12",
    name := "basic",
    version := "0.1.0-SNAPSHOT",
    description := "Basic test for the shocon sbt plugin",
    libraryDependencies += "org.akka-js" %%% "shocon" % sys.props.getOrElse("plugin.version", sys.error("'plugin.version' environment variable is not set")),
    compile in Compile := (compile in Compile).dependsOn(shoconConcat).value
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
  .nativeSettings(
    nativeGC := "boehm",
    nativeMode := "debug",
    nativeLinkStubs := false
  )

lazy val rootJVM = root.jvm
lazy val rootJS = root.js
lazy val rootNative = root.native

lazy val lib = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file("lib"))
  .enablePlugins(ShoconPlugin)
  .settings(
    scalaVersion := "2.11.12",
    name := "lib"
  )

lazy val libJVM = lib.jvm
lazy val libJS = lib.js
lazy val libNative = lib.native
