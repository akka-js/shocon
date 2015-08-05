package eu.unicredit.shocon

import org.parboiled2._

import scala.util.Try

class ConfigParser(val input: ParserInput) extends Parser {
  def InputLine = rule { ConfigElement ~ EOI }
  def ConfigElement: Rule1[Ast.Value] = rule {
      KeyValue | Object

  }

  def Array: Rule1[Ast.Array] = rule {
    wscn~'[' ~ zeroOrMore(Value ~ zeroOrMore(valuecomments)).separatedBy(anyOf(",\n")) ~  wscn~']' ~> { (x: Seq[Ast.Value]) => Ast.Array(x) }
  }

  def valuecomments = rule { ws~(singlelinecomment) }

  def Object: Rule1[Ast.Object] = rule {
    wscn~'{' ~ zeroOrMore(KeyValue).separatedBy(anyOf(",\n")) ~ wscn~'}'~wsc ~> { (x: Seq[Ast.KeyValue]) => Ast.Object( x.map( (kv: Ast.KeyValue) => (kv.key,kv.value) ).toMap ) }
  }

  def KeyValue: Rule1[Ast.KeyValue] = rule { wscn ~
    ( (Key ~ ':' ~ Value)
    | (Key ~ '=' ~ Value)
    | (Key ~ Array)
    | (Key ~ Object) ) ~> { (x:Ast.StringLiteral,y:Ast.Value) => Ast.KeyValue(x.value,y) }
  }

  def Key: Rule1[Ast.StringLiteral] = rule {
    StringLiteral ~ wsc
  }

  def Value : Rule1[Ast.Value] = rule {
    wscn ~( SimpleValue | Array | Object )
  }

  def SimpleValue: Rule1[Ast.SimpleValue] = rule {
    wsc ~( Number  | StringLiteral ~> { (s:Ast.StringLiteral) => Try(Ast.BooleanLiteral(s.value.toBoolean)).getOrElse(s) } )
  }

  def StringLiteral: Rule1[Ast.StringLiteral] = rule {
    QuotedString | UnquotedString
  }

  def UnquotedString: Rule1[Ast.StringLiteral] = rule {
    capture(oneOrMore(CharPredicate.AlphaNum)) ~> { s => Ast.StringLiteral(s) }
  }

  def QuotedString: Rule1[Ast.StringLiteral] = rule {
   Quote ~ capture(zeroOrMore(!Quote  ~ ANY)) ~ Quote ~> Ast.StringLiteral
  }

  val Quote = "\""

//  val NormalChar = rule { noneOf("\"") }



  def Number: Rule1[Ast.NumberLiteral] = rule {
    capture(oneOrMore(CharPredicate.Digit)) ~> { n => Ast.NumberLiteral(n) }
  }
//  def Boolean: Rule1[Ast.BooleanLiteral] = rule {
//    capture("true" | "false") ~> { (b:String) => Ast.BooleanLiteral (b) }
//  }


  implicit def wsp(c: Char): Rule0 = rule {
    str(c.toString) ~ zeroOrMore(anyOf(" \r\t"))
  }
  def ws: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r"))
  }
  def wsc: Rule0 = rule {
    ws
  }
  def wscn: Rule0 = rule {
    wsn ~ optional((singlelinecomment) ~ wsn)
  }
  def wsn: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r\n"))
  }
  def singlelinecomment: Rule0 = rule {
    ("//"| "#") ~zeroOrMore(!"\n"~ANY)
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

  case class BooleanLiteral(value: Boolean) extends SimpleValue

  case object NullLiteral extends SimpleValue

}