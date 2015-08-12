package eu.unicredit.shocon

import fastparse.all._

import scala.util.Try

object ConfigParser {

  val Whitespace_n = NamedFunction(" \n".contains(_: Char), "Whitespace_n")
  val wspn  = P( CharsWhile(Whitespace_n).? )

  val Whitespace = NamedFunction(" ".contains(_: Char), "Whitespace")
  val wsp   = P( CharsWhile(Whitespace).? )

  val InputLine = P { ConfigElement  ~ End }
  val ConfigElement = P {
    wspn ~  ( Object | ObjectBody )
  }
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V){
      def apply(t: T) = f(t)
      override def toString() = name
  }


  val StringChars = NamedFunction(!"\"\\".contains(_: Char), "StringChars")

  val strChars = P( CharsWhile(StringChars) )
  val escape   = P( "\\" ~ (CharIn("\"/\\bfnrt") /* | unicodeEscape */)  )


  val quotedString = P( "\"" ~! (strChars | escape).rep.! ~ "\"" )
  val bareString = P ( CharsWhile { (c:Char) => ! Whitespace_n(c) && ! ":=,{}[]".contains(c) }.!  )

  val string: Parser[Config.StringLiteral] = P( (quotedString|bareString)).map(Config.StringLiteral)

  val Key =  P ( string.map{_.value} )

  val KeyValue = P ( Key ~! ( ( KeyValueSeparator ~ Value) | StructuredValue ) )

  val ObjectBody: Parser[Config.Object] =  P ( KeyValue.rep(sep=ListSeparator) ).map{ _.toMap } .map (Config.Object(_))

  val Array: Parser[Config.Array] = P {
    wspn_("[") ~ ArrayBody ~  wspn_("]")
  }

  val ArrayBody : Parser[Config.Array] = P {
    Value.rep(sep=ListSeparator) map { (x: Seq[Config.Value]) => Config.Array(x) }
  }

  val Object = P {
    wspn_("{") ~ ObjectBody ~ wspn_("}")
  }
  val ListSeparator = P ( CharsWhile( " \t\r\n,".contains(_:Char) ) )
  val KeyValueSeparator = P ( wsp ~ CharIn(":=") ~ wspn )
  //
  // def KeyValue: Rule1[Config.KeyValue] = rule {
  //   ( (Key ~ wsp_(":") ~ (Value))
  //   | (Key ~ wsp_("=") ~ (Value))
  //   | (Key ~ Array)
  //   | (Key ~ Object) ) ~> { (x:Config.StringLiteral,y:Config.Value) => Config.KeyValue(x.value,y) }
  // }
  //
  // def Key: Rule1[Config.StringLiteral] = rule {
  //   StringLiteral ~ ws
  // }
  //
  val Value : Parser[Config.Value] = P {
    wsp_(string) | StructuredValue
  }

  val StructuredValue: Parser[Config.Value] = P {
     Array | Object
  }
  //
  // val SimpleValue: Parser[Config.SimpleValue] = P {
  //   ( StringLiteral map { (s:Config.StringLiteral) => Try(Config.BooleanLiteral(s.value.toBoolean)).getOrElse(s) } )
  // }
  //
  // def StringLiteral: Rule1[Config.StringLiteral] = rule {
  //   QuotedString | UnquotedString
  // }
  //
  // def UnquotedString: Rule1[Config.StringLiteral] = rule {
  //   capture(oneOrMore(IdentifierChar ++ ' ' ++ '\t')) ~> { s:String => Config.StringLiteral(s.trim()) }
  // }
  //
  // def QuotedString: Rule1[Config.StringLiteral] = rule {
  //  Quote ~ capture(zeroOrMore(!Quote  ~ ANY)) ~ Quote ~> Config.StringLiteral
  // }
  //
  // val Quote = "\""
  //
  // def Identifier = rule { capture(oneOrMore(IdentifierChar)) }
  //
  // val WhiteSpaceChar = CharPredicate(" \n\r\t\f")
  // //val IdentifierFirstChar = CharPredicate.Alpha ++ '_' ++ '-' ++ '.'
  // val IdentifierChar = CharPredicate.Alpha ++ '_' ++ '-' ++ '.' ++ CharPredicate.Digit
  //
  //
  // def Number: Rule1[Config.NumberLiteral] = rule {
  //   capture(optional('-') ~ oneOrMore(CharPredicate.Digit ++ '.')) ~> { n => Config.NumberLiteral(n) }
  // }
  //
  def wsp_[T](s:Parser[T]): Parser[T] = P ( s ~ wsp )
  def wspn_[T](s:Parser[T]): Parser[T] = P ( s ~ wspn )
  // def wsn: Rule0 = rule {
  //   zeroOrMore(anyOf(" \t\r\n") | Comment)
  // }
  // def ws: Rule0 = rule {
  //   zeroOrMore(anyOf(" \t\r"))
  // }
  // def Comment: Rule0 = rule {
  //   ("//"| "#") ~zeroOrMore(!"\n"~ANY)
  // }

}
