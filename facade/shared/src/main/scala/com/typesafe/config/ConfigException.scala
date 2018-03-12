package com.typesafe.config

/**
  * Created by evacchi on 27/11/15.
  */

abstract class ConfigException(message: String, cause: Throwable) extends RuntimeException(message,cause) {
  def this(message: String) = this(message,null)
}

object ConfigException {
  case class Missing(path: String) extends ConfigException(path)
  case class BadValue(path: String) extends ConfigException(path)
  case class BugOrBroken(message: String) extends ConfigException(message)
}
