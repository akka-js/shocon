package com.typesafe.config

class ConfigValueType private() {}
object ConfigValueType {
  val OBJECT, LIST, NUMBER, BOOLEAN, NULL, STRING = new ConfigValueType()
}
