package eu.unicredit.shocon

import org.parboiled2._

import scala.util.Try

class ConfigParser(val input: ParserInput) extends Parser {
  def InputLine = rule { ConfigElement  ~ EOI }
  def ConfigElement: Rule1[Ast.Object] = rule {
      wsn ~ ( Object | ObjectBody )
  }

  def Array: Rule1[Ast.Array] = rule {
    wspn_("[") ~ ArrayBody ~  wspn_("]")
  }

  def ArrayBody : Rule1[Ast.Array] = rule {
    zeroOrMore(Value).separatedBy(Separator) ~> { (x: Seq[Ast.Value]) => Ast.Array(x) }
  }

  def Object: Rule1[Ast.Object] = rule {
    wspn_("{") ~ObjectBody ~ wspn_("}")
  }

  def ObjectBody: Rule1[Ast.Object] = rule {
     zeroOrMore(KeyValue).separatedBy(Separator) ~> { (x: Seq[Ast.KeyValue]) => Ast.Object( x.map( (kv: Ast.KeyValue) => (kv.key,kv.value) ).toMap ) }
  }

  def Separator = rule { 
    zeroOrMore( anyOf(" \t\r\n,") | Comment )
   }

  def KeyValue: Rule1[Ast.KeyValue] = rule { 
    ( (Key ~ wsp_(":") ~ (Value))
    | (Key ~ wsp_("=") ~ (Value))
    | (Key ~ Array)
    | (Key ~ Object) ) ~> { (x:Ast.StringLiteral,y:Ast.Value) => Ast.KeyValue(x.value,y) }
  }

  def Key: Rule1[Ast.StringLiteral] = rule {
    StringLiteral ~ ws
  }

  def Value : Rule1[Ast.Value] = rule {
    (wsn~SimpleValue~wsn) | ( Array | Object )
  }

  def SimpleValue: Rule1[Ast.SimpleValue] = rule {
    ( Number  | StringLiteral ~> { (s:Ast.StringLiteral) => Try(Ast.BooleanLiteral(s.value.toBoolean)).getOrElse(s) } )
  }

  def StringLiteral: Rule1[Ast.StringLiteral] = rule {
    QuotedString | UnquotedString
  }

  def UnquotedString: Rule1[Ast.StringLiteral] = rule {
    ((Identifier)) ~> { s => Ast.StringLiteral(s) }
  }

  def QuotedString: Rule1[Ast.StringLiteral] = rule {
   Quote ~ capture(zeroOrMore(!Quote  ~ ANY)) ~ Quote ~> Ast.StringLiteral
  }

  val Quote = "\""

  def Identifier = rule { capture(IdentifierFirstChar ~ zeroOrMore(IdentifierChar)) }

  val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  val IdentifierFirstChar = CharPredicate.Alpha ++ '_' 
  val IdentifierChar = IdentifierFirstChar ++ CharPredicate.Digit ++ '-'


  def Number: Rule1[Ast.NumberLiteral] = rule {
    capture(optional('-') ~ oneOrMore(CharPredicate.Digit ++ '.')) ~> { n => Ast.NumberLiteral(n) }
  }

  def wsp_(s:String): Rule0 = rule {
    s~ws 
  }
  def wspn_(s:String): Rule0 = rule {
    s~wsn 
  }
  def wsn: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r\n") | Comment)
  }
  def ws: Rule0 = rule {
    zeroOrMore(anyOf(" \t\r"))
  }
  def Comment: Rule0 = rule {
    ("//"| "#") ~zeroOrMore(!"\n"~ANY)
  }

}
