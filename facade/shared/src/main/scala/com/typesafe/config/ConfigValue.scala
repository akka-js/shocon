package com.typesafe.config

import org.akkajs.shocon

trait ConfigValue extends ConfigMergeable {
  val inner: shocon.Config.Value
  def render(): String = inner.toString
  def valueType(): ConfigValueType = inner match {
    case _: shocon.Config.Object => ConfigValueType.OBJECT
    case _: shocon.Config.Array => ConfigValueType.LIST 
    case _: shocon.Config.NumberLiteral => ConfigValueType.NUMBER 
    case _: shocon.Config.StringLiteral => ConfigValueType.STRING 
    case _: shocon.Config.BooleanLiteral => ConfigValueType.BOOLEAN 
    case _: shocon.Config.NullLiteral.type => ConfigValueType.NULL 
  }
}
