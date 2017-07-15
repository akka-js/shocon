# SHocon

[![Build Status](https://travis-ci.org/unicredit/shocon.png?branch=master)](https://travis-ci.org/unicredit/shocon)
[![Latest version](https://index.scala-lang.org/unicredit/shocon/shocon/latest.svg?color=orange)](https://index.scala-lang.org/unicredit/shocon/shocon)

SHocon is a simple, pure-Scala, alternative implementation of the [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) 
specification.

SHocon ships with a native, Scala-idiomatic API, and a shim that mimics the [Typesafe Config](https://github.com/typesafehub/config) Java API, making it well-suited as a **drop-in replacement** wherever the Java implementation is not available, such as **Scala.JS** projects.

This implementation does not cover all of the corner cases of the original implementation. Issues and PRs are welcome!

## Usage

Add these lines to your build.sbt

**Scala project**:
```scala
libraryDependencies += "eu.unicredit" %% "shocon" % "0.1.9-SNAPSHOT"
```

**Scala.js project**:

In `project/plugins.sbt`:
```scala
addSbtPlugin("eu.unicredit" % "sbt-shocon" % "0.1.9-SNAPSHOT")
```

and in `build.sbt`:
```scala
val root = project.in(file(".")
  .enablePlugins(ScalaJSPlugin,ShoconJSPlugin)
  /* ... */
```

## Notes

### JVM
For JVM project a call to `ConfigFactory.load()` will load the configuration from all `reference.conf` and `application.conf` files found on the classpath at runtime (the same as Typesafe config does).

### JS
Since in a Scala.js project there are no runtime classloaders/ resources, the default configuration returned by `ConfigFactory.load()` is compiled statically into the code at compile time. This includes all `reference.conf` files found in the `resources` directory of the project itself, as well as all `reference.conf` files found in JARs on which the project depends. If there is an `application.conf` file in the `resources` directory of the project, this one will be included as well (after all `reference.conf` files.

The resulting HOCON configuration file is assembled in `target-SCALA_VERSION/shocon.conf`.

### ShoconJSPlugin setttings
You can control the contents of the included default configuration with the following sbt settings:

* `shoconLoadFromJars`: set to false, if you don't want to include any `reference.conf` files found in JARs
* `shoconFilter: Function1[(String,InputStream), Boolean]`: set this setting to a filter function that return `true` for all configuration files to be included; the first element in the tuple passed to the function is the absolute URL of the configuration file.
