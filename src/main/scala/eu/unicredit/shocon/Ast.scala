package eu.unicredit.shocon

object Config {
  
  type Key = String

  sealed trait Value

  case class Array(elements: Seq[Value]) extends Value

  case class Object(fields: Map[Key, Value]) extends Value
  case class KeyValue(key: String, value: Value) extends Value


  trait SimpleValue extends Value

  case class NumberLiteral(value: String) extends SimpleValue

  case class StringLiteral(value: String) extends SimpleValue

  case class BooleanLiteral(value: Boolean) extends SimpleValue

  case object NullLiteral extends SimpleValue

}