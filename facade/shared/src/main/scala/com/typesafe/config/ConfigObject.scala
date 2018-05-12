package com.typesafe.config

import java.{util => ju}

import scala.collection.JavaConverters._

trait ConfigObject extends ju.AbstractMap[String, ConfigValue] with ConfigValue {

	def toConfig = Config(inner)
  def unwrapped: ju.Map[String, Any]

  override def render: String = this.entrySet().asScala.map(kv => kv.getKey+" -> "+kv.getValue).mkString("[", ",", "]")
  override def valueType: ConfigValueType = ConfigValueType.OBJECT

}
