package com.typesafe.config

trait ConfigMergeable {
  def withFallback(other: ConfigMergeable): ConfigMergeable = ???
}
