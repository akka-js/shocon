# SHocon

[![Build Status](https://travis-ci.org/akka-js/shocon.png?branch=master)](https://travis-ci.org/akka-js/shocon)

SHocon is a simple, pure-Scala, alternative implementation of the [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)
specification.

SHocon ships with a native, Scala-idiomatic API, and a shim that mimics the [Typesafe Config](https://github.com/typesafehub/config) Java API, making it well-suited as a **drop-in replacement** wherever the Java implementation is not available, such as **Scala.JS** or **Scala Native** projects.

This implementation does not cover all of the corner cases of the original implementation. Issues and PRs are welcome!

## Usage

Add these lines to your `project/plugins.sbt`:
```scala
addSbtPlugin("org.akka-js" % "sbt-shocon" % "1.0.0")
```

and in `build.sbt`:
```scala
val root = project.in(file("."))
  .enablePlugins(ShoconPlugin)
  .settings(
    libraryDependencies += "org.akka-js" %% "shocon" % "1.0.0",
    // for Scala.js/Native or cross projects use %%% instead:
    // libraryDependencies += "org.akka-js" %%% "shocon" % "1.0.0"

    // add dependency on shocon file generation task
    // (not required, but otherwise you need to call shoconConcat manually before compilation!)
    compile in Compile := (compile in Compile).dependsOn(shoconConcat).value

    /* ... */
  )
```

## Credits

SHocon wouldn't have been possible without the enormous support of the R&D department of UniCredit lead by Riccardo Prodam. Started as a side-project it quickly grew into an important open source milestone.
Check out other projects from the UniCredit team [here](https://github.com/unicredit)

## Notes

### Scala.Js support

Starting from shocon `1.0.0` we dropped support for Scala.Js `0.6`, the latest artifact published for Scala.Js `0.6` is Shocon `0.5.0`

### Loading of default configuration
In contrast to Typesafe config, which loads configuration files dynamically at run time, shocon compiles the default configuration returned by `ConfigFactory.load()` statically into the the code. This includes all `reference.conf` files found in the `resources` directory of the project itself, as well as all `reference.conf` files found in JARs on which the project depends. If there is an `application.conf` file in the `resources` directory of the project, this one will be included as well (after all `reference.conf` files).

The resulting HOCON configuration file is assembled in `target/scala-VERSION/shocon.conf`.

*Note*: For Scala.JS / Native / JVM projects only the `reference.config` files located in either `js/src/main/resources` and `jvm/src/main/resources` are included; files in `shared/src/main/resources/` are ignored!

Since version `0.3.1` the parse phase is aggressively moved at compile time, please note that runtime parsing cost a lot in terms of performances.

### ShoconPlugin settings
You can control the contents of the included default configuration with the following sbt settings:

* `shoconLoadFromJars`: set to false, if you don't want to include any `reference.conf` files found in JARs
* `shoconFilter: Function1[(String,InputStream), Boolean]`: set this setting to a filter function that return `true` for all configuration files to be included; the first element in the tuple passed to the function is the absolute URL of the configuration file.
