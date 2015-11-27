package com.typesafe.config

/**
  * Created by evacchi on 27/11/15.
  */

abstract class ConfigException extends RuntimeException
object ConfigException {
  case class Missing(path: String) extends ConfigException
  case class BadValue(path: String) extends ConfigException
}
