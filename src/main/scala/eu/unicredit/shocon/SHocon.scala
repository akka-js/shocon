package eu.unicredit

import org.parboiled2._
import scala.util.Try


package object shocon extends Extractors {

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

    def parse(input: ParserInput) = new ConfigParser(input).InputLine.run()
    def apply(input: ParserInput) = parse(input).get // new Config(input)
    def fromFile(path: String) = apply(io.Source.fromFile(path).mkString)
  }

  
  implicit class ConfigOps(val tree:  Config.Value) {
    def as[T](implicit ev: Extractor[T]): Option[T] = Option( ev.applyOrElse(tree, null) ) 
    def apply(key: String): Config.Value = get(key).get
    def get(key: String): Option[Config.Value] = {
      val keys = key.split('.')
      def visit(v:  Config.Value, keys: Seq[String]): Option[Config.Value] = v match {
        case _ if (keys.isEmpty)     => Some(v)
        case o@Config.Object(fields) =>
            if (fields.contains(keys.head)) 
              visit(fields(keys.head), keys.tail)
            else None
      }
      visit(tree, keys)
    }

    // def getOrElse[T](fallback: => Config.Value)(implicit ev: Extractor[T]): T = 
    //   apply(key)(ev).getOrElse(fallback.get(key)(ev))
  }

}
