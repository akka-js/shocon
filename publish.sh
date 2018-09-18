#! /bin/sh

sbt clean

sbt ";++2.11.11;facadeJS/publishSigned;facadeJVM/publishSigned;facadeNative/publishSigned;parserJS/publishSigned;parserJVM/publishSigned;parserNative/publishSigned"

sbt ";++2.12.6;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"

sbt sonatypeReleaseAll

cd plugin

sbt ";^^0.13.17;publishSigned"

sbt ";^^1.2.1;publishSigned"

sbt sonatypeReleaseAll

cd ..
