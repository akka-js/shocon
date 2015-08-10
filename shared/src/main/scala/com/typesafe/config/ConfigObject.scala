package com.typesafe.config

import java.{util => ju}

trait ConfigObject extends ju.AbstractMap[String, ConfigValue] with ConfigValue {

	def toConfig = Config(inner)

}
