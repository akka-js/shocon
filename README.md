# SHocon

[![Build Status](https://travis-ci.org/unicredit/shocon.png?branch=master)](https://travis-ci.org/unicredit/shocon)
[![Latest version](https://index.scala-lang.org/unicredit/shocon/shocon/latest.svg?color=orange)](https://index.scala-lang.org/unicredit/shocon/shocon)

SHocon is a simple, pure-Scala, alternative implementation of the [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) 
specification.

SHocon ships with a native, Scala-idiomatic API, and a shim that mimics the [Typesafe Config](https://github.com/typesafehub/config) Java API, making it well-suited as a **drop-in replacement** wherever the Java implementation is not available, such as **Scala.JS** projects.

This implementation does not cover all of the corner cases of the original implementation. Issues and PRs are welcome!

## Usage

Add these lines to your build.sbt

Scala project:
```scala
libraryDependencies += "eu.unicredit" %% "shocon" % "0.1.8"
```

Scala.Js project:
```scala
libraryDependencies += "eu.unicredit" %%% "shocon" % "0.1.8"
```

## Notes

Since this implementation is unaware to be on a JVM classloader, any such reference is ignored and will be `null`.

Default configuration is loaded from "application.conf" in resources statically at compile time (using macros) and integrated as a string in target code.
