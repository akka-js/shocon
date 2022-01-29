#! /bin/sh

sbt clean

sbt ";++2.12.15;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt ";++2.13.8;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt sonatypeReleaseAll

cd plugin

sbt ";^^1.3.13;publishSigned"

sbt sonatypeReleaseAll

cd ..
