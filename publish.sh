#! /bin/sh

sbt clean

sbt ";++2.12.10;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt ";++2.13.1;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt sonatypeReleaseAll

cd plugin

sbt ";^^0.13.18;publishSigned"

sbt ";^^1.2.8;publishSigned"

sbt sonatypeReleaseAll

cd ..
