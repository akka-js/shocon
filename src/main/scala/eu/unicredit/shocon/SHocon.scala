package eu.unicredit.shocon

import org.parboiled2._

class ConfigParser(val input: ParserInput) extends Parser {
  def InputLine = rule { ConfigElement ~ EOI }
  def ConfigElement = rule {
    Array | Object
  }
  def Array = rule {
    '[' ~ Value ~ zeroOrMore(',' ~ Value) ~ ']'
  }
  def Object = rule {
    '{' ~ KeyValue ~ zeroOrMore(',' ~ KeyValue )~ '}'
  }

  def KeyValue = rule {
    (Key ~ ':' ~ Value) ~> { (x,y) => (x,y) }
  }
  def Key = rule {
    StringLiteral
  }
  def Value = rule {
    SimpleValue
  }
  def SimpleValue = rule {
    Number
  }

  def StringLiteral = rule {
    capture(oneOrMore(CharPredicate.AlphaNum))
  }
  def Number = rule {
    capture(oneOrMore(CharPredicate.Digit))
  }



//  def Expression: Rule1[Int] = rule {
//    Term ~ zeroOrMore(
//      '+' ~ Term ~> ((_: Int) + _)
//        | '-' ~ Term ~> ((_: Int) - _))
//  }
//
//  def Term = rule {
//    Factor ~ zeroOrMore(
//      '*' ~ Factor ~> ((_: Int) * _)
//        | '/' ~ Factor ~> ((_: Int) / _))
//  }
//
//  def Factor = rule { Number | Parens }
//
//  def Parens = rule { '(' ~ Expression ~ ')' }
//
//  def Number = rule { capture(Digits) ~> (_.toInt) }
//
//  def Digits = rule { oneOrMore(CharPredicate.Digit) }
}

object Ast {
  type Key = String
  type Field = (Key, Value)


  case class Config(root: ConfigElement)

  sealed trait ConfigElement

  case class Array(elements: List[ConfigElement]) extends ConfigElement

  case class Object(fields: Map[Key, Value]) extends ConfigElement

  sealed trait Value

  trait SimpleValue extends Value


  sealed trait BooleanLiteral extends SimpleValue

  case class NumberLiteral(value: Double) extends SimpleValue

  case class StringLiteral(value: String) extends SimpleValue

  case object True extends BooleanLiteral

  case object False extends BooleanLiteral

  case object NullLiteral extends SimpleValue

}