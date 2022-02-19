#!/bin/sh

if [ -n "$SBT_VERSION" ]; then
  sbt ++"$TRAVIS_SCALA_VERSION" publishLocal
  cd plugin || exit 1
  sbt ^^"$SBT_VERSION" 'scripted shocon/basic'
fi;
