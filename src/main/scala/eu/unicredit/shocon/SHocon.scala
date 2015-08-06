package eu.unicredit

import org.parboiled2._


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

  
  implicit class RichAst(val tree:  Config.Value) {
    def get[T](key: String)(implicit ev: Extractor[T]) = {
      val keys = key.split('.')
      def visit(v:  Config.Value, keys: Seq[String]): T = v match {
          case _ if (keys.isEmpty)     => ev.apply(v)
          case o@Config.Object(fields) => visit(fields(keys.head), keys.tail)
        }
      visit(tree, keys)
    }
  }

}
