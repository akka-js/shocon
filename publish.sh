#! /bin/sh

sbt clean

sbt ";++2.12.15;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"
sbt ";++2.13.8;facadeJVM/publishLocal;parserJVM/publishLocal;facadeJS/publishSigned;facadeJVM/publishSigned;parserJS/publishSigned;parserJVM/publishSigned"
sbt sonatypeReleaseAll

cd plugin || exit 1

sbt ";^^1.4.9;publishSigned"
sbt sonatypeReleaseAll

cd ..
