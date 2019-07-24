#! /bin/sh

sbt clean

sbt ";++2.12.8;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt ";++2.13.0;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt sonatypeReleaseAll

cd plugin

sbt ";^^0.13.17;publishSigned"

sbt ";^^1.2.1;publishSigned"

sbt sonatypeReleaseAll

cd ..
