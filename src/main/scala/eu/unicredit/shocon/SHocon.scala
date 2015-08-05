package eu.unicredit.shocon

import org.parboiled2._

class ConfigParser(val input: ParserInput) extends Parser {
  def InputLine = rule { ConfigElement ~ EOI }
  def ConfigElement: Rule1[Ast.Value] = rule {
      KeyValue | Object

  }

  def Array: Rule1[Ast.Array] = rule {
    '[' ~ zeroOrMore(Value).separatedBy(anyOf(",\n")) ~  ']' ~> { (x: Seq[Ast.Value]) => Ast.Array(x) }
  }

  def Object: Rule1[Ast.Object] = rule {
    '{' ~ zeroOrMore(KeyValue).separatedBy(anyOf(",\n")) ~ wsn~'}' ~> { (x: Seq[Ast.KeyValue]) => Ast.Object( x.map( (kv: Ast.KeyValue) => (kv.key,kv.value) ).toMap ) }
  }

  def KeyValue: Rule1[Ast.KeyValue] = rule { wsn ~
    ( (Key ~ ':' ~ Value)
    | (Key ~ '=' ~ Value) ) ~> { (x:Ast.StringLiteral,y:Ast.Value) => Ast.KeyValue(x.value,y) }
  }

  def Key: Rule1[Ast.StringLiteral] = rule {
    StringLiteral ~ ws
  }

  def Value : Rule1[Ast.Value] = rule {
    wsn ~( SimpleValue | Array | Object )
  }

  def SimpleValue: Rule1[Ast.SimpleValue] = rule {
    ws ~(Number | StringLiteral | Boolean)
  }

  def StringLiteral: Rule1[Ast.StringLiteral] = rule {
    capture(oneOrMore(CharPredicate.AlphaNum)) ~> { s => Ast.StringLiteral(s) }
  }
  def Number: Rule1[Ast.NumberLiteral] = rule {
    capture(oneOrMore(CharPredicate.Digit)) ~> { n => Ast.NumberLiteral(n) }
  }
  def Boolean: Rule1[Ast.BooleanLiteral] = rule {
    capture("true" | "false") ~> { (b:String) => Ast.BooleanLiteral (b) }
  }


  implicit def wsp(c: Char): Rule0 = rule {
    str(c.toString) ~ zeroOrMore(anyOf(" \n\r\t"))
  }
  def ws: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r"))
  }
  def wsn: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r\n"))
  }


}

object Ast {
  type Key = String
  type Field = (Key, Value)


  case class Config(root: Value)

  sealed trait Value

  case class Array(elements: Seq[Value]) extends Value

  case class Object(fields: Map[Key, Value]) extends Value
  case class KeyValue(key: String, value: Value) extends Value


  trait SimpleValue extends Value



  case class NumberLiteral(value: String) extends SimpleValue

  case class StringLiteral(value: String) extends SimpleValue

  case class BooleanLiteral(value: String) extends SimpleValue

  case object NullLiteral extends SimpleValue

}