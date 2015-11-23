package eu.unicredit.shocon

import fastparse.all._

object ConfigParser {
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V){
    def apply(t: T) = f(t)
    override def toString() = name

  }


  // Here is the parser
  val Whitespace = NamedFunction(" \n".contains(_: Char), "Whitespace")
  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val StringChars = NamedFunction(!"\"\\".contains(_: Char), "StringChars")

  val space         = P( CharsWhile(Whitespace).? )
  val digits        = P( CharsWhile(Digits))
  val exponent      = P( CharIn("eE") ~ CharIn("+-").? ~ digits )
  val fractional    = P( "." ~ digits )
  val integral      = P( "0" | CharIn('1' to '9') ~ digits.? )

  val number = P( CharIn("+-").? ~ integral ~ fractional.? ~ exponent.? ).!.map(
    x => Config.NumberLiteral(x)
  )

  val `null`        = P( "null" ).map(_ => Config.NullLiteral)
  val `false`       = P( "false" ).map(_ => Config.BooleanLiteral(false))
  val `true`        = P( "true" ).map(_ => Config.BooleanLiteral(true))

  val hexDigit      = P( CharIn('0'to'9', 'a'to'f', 'A'to'F') )
  val unicodeEscape = P( "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit )
  val escape        = P( "\\" ~ (CharIn("\"/\\bfnrt") | unicodeEscape) )

  val strChars = P( CharsWhile(StringChars) )
  val string =
    P( space ~ "\"" ~/ (strChars | escape).rep.! ~ "\"")
      .map(s => Config.StringLiteral(s))

  val array =
    P( "[" ~/ jsonExpr.rep(sep=",".~/) ~ space ~ "]").map( x => Config.Array(x) )

  val pair = P( string.map(_.value) ~/ ":" ~/ jsonExpr )

  val obj =
    P( "{" ~/ pair.rep(sep=",".~/) ~ space ~ "}").map( x => Config.Object(Map(x:_*)) )

  val jsonExpr: P[Config.Value] = P(
    space ~ (obj | array | string | `true` | `false` | `null` | number) ~ space
  )

}
