# SHocon

[![Build Status](https://travis-ci.org/unicredit/shocon.png?branch=master)](https://travis-ci.org/unicredit/shocon)

A simple pure Scala implementation of the [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) specification that (mostly*) expose the same api as [Typesafe Config](https://github.com/typesafehub/config).

This library is intended to be used as a drop-in alternative to Typesafe Config expecially within Scala.JS projects.

Please consider that this implementation does not cover the total number of corner cases issues and PRs are welcome!

##Use It

add this lines to your build.sbt

Scala project:
```scala
libraryDependencies += "eu.unicredit" %% "shocon" % "0.1.4"
```

Scala.Js project:
```scala
libraryDependencies += "eu.unicredit" %%% "shocon" % "0.1.4"
```

## Notes

Since this implementation is un-aware to be on a JVM classloader is ignored everywhere and will be ```null```.

Default configuration is loaded just from file "application.conf" in resources statically at compile time (using macro) and integrated as a string in target code.
