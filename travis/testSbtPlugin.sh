#!/bin/sh

if [ ! -z $SBT_VERSION ]; then source test_plugin.sh
  sbt ++$TRAVIS_SCALA_VERSION publishLocal
  cd plugin
  sbt ^^$SBT_VERSION 'scripted shocon/basic'
fi;
