# SHocon

A simple pure-Scala implementation of the HOCON file format.

This library is intended to be used as a drop-in alternative to Typesafe Config expecially with Scala.JS projects.

Default configuration is now loaded statically at compile time and integrated as a string in target code.

##Use It

add this lines to your build.sbt
```
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "eu.unicredit" %%% "shocon" % "0.0.3-SNAPSHOT"
```
