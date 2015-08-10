package eu.unicredit.shocon

import org.parboiled2._

import scala.util.Try

class ConfigParser(val input: ParserInput) extends Parser {

  def InputLine = rule { ConfigElement  ~ EOI }
  def ConfigElement: Rule1[Config.Object] = rule {
      wsn ~ ( Object | ObjectBody )
  }

  def Array: Rule1[Config.Array] = rule {
    wspn_("[") ~ ArrayBody ~  wspn_("]")
  }

  def ArrayBody : Rule1[Config.Array] = rule {
    zeroOrMore(Value).separatedBy(Separator) ~> { (x: Seq[Config.Value]) => Config.Array(x) }
  }

  def Object: Rule1[Config.Object] = rule {
    wspn_("{") ~ObjectBody ~ wspn_("}")
  }

  def ObjectBody: Rule1[Config.Object] = rule {
     zeroOrMore(KeyValue).separatedBy(Separator) ~> { (x: Seq[Config.KeyValue]) => Config.Object( x.map( (kv: Config.KeyValue) => (kv.key,kv.value) ).toMap ) }
  }

  def Separator = rule { 
    zeroOrMore( anyOf(" \t\r\n,") | Comment )
   }

  def KeyValue: Rule1[Config.KeyValue] = rule { 
    ( (Key ~ wsp_(":") ~ (Value))
    | (Key ~ wsp_("=") ~ (Value))
    | (Key ~ Array)
    | (Key ~ Object) ) ~> { (x:Config.StringLiteral,y:Config.Value) => Config.KeyValue(x.value,y) }
  }

  def Key: Rule1[Config.StringLiteral] = rule {
    StringLiteral ~ ws
  }

  def Value : Rule1[Config.Value] = rule {
    (wsn~SimpleValue~wsn) | ( Array | Object )
  }

  def SimpleValue: Rule1[Config.SimpleValue] = rule {
    ( StringLiteral ~> { (s:Config.StringLiteral) => Try(Config.BooleanLiteral(s.value.toBoolean)).getOrElse(s) } )
  }

  def StringLiteral: Rule1[Config.StringLiteral] = rule {
    QuotedString | UnquotedString
  }

  def UnquotedString: Rule1[Config.StringLiteral] = rule {
    capture(oneOrMore(IdentifierChar ++ ' ' ++ '\t')) ~> { s:String => Config.StringLiteral(s.trim()) }
  }

  def QuotedString: Rule1[Config.StringLiteral] = rule {
   Quote ~ capture(zeroOrMore(!Quote  ~ ANY)) ~ Quote ~> Config.StringLiteral
  }

  val Quote = "\""

  def Identifier = rule { capture(oneOrMore(IdentifierChar)) }

  val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  //val IdentifierFirstChar = CharPredicate.Alpha ++ '_' ++ '-' ++ '.'
  val IdentifierChar = CharPredicate.Alpha ++ '_' ++ '-' ++ '.' ++ CharPredicate.Digit 


  def Number: Rule1[Config.NumberLiteral] = rule {
    capture(optional('-') ~ oneOrMore(CharPredicate.Digit ++ '.')) ~> { n => Config.NumberLiteral(n) }
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
