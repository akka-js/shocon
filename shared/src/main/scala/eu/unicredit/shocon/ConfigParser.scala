package eu.unicredit.shocon

import fastparse.all._

object ConfigParser {
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V){
    def apply(t: T) = f(t)
    override def toString() = name

  }


  // Here is the parser
  val Whitespace = NamedFunction(" \n".contains(_: Char), "Whitespace")
  val letter     = P( lowercase | uppercase )
  val lowercase  = P( CharIn('a' to 'z') )
  val uppercase  = P( CharIn('A' to 'Z') )
  val digit      = P( CharIn('0' to '9') )
  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val StringChars = NamedFunction(!"\"\\".contains(_: Char), "StringChars")
  val UnquotedStringChars = NamedFunction(!Whitespace(_: Char), "UnquotedStringChars  ")

  val wspace        = P( CharsWhile(Whitespace) )
  val digits        = P( CharsWhile(Digits))
  val exponent      = P( CharIn("eE") ~ CharIn("+-").? ~ digits )
  val fieldSep      = P( CharIn(":="))
  val fractional    = P( "." ~ digits )
  val integral      = P( "0" | CharIn('1' to '9') ~ digits.? )

  //val comment       = P( ("#"|"//") ~/ CharsWhile(! "\n" .contains (_: Char) ) ~/ "\n" )

  val comment = P( "#" ~ CharsWhile(_ != '\n', min = 0) )
  val space = P( (CharsWhile(" \n".toSet, min = 1) | comment | "\\\n").rep )
  val nonewlinewscomment = P( (CharsWhile(" ".toSet, min = 1) | comment | "\\\n").rep )


  // val space         = P( wspace.? ~ (comment ~ wspace.?).rep(min = 0) )

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
  val quotedString =
    P( space ~ "\"" ~/ (strChars | escape).rep.! ~ "\"").map(s => Config.StringLiteral(s))

  val unquotedString: P[Config.StringLiteral] =
    P ( space ~ (letter|"_") ~ (letter | digit | "_" | "-").rep(min=0).!)
    .map(Config.StringLiteral)

  val string = P(unquotedString)


  //val string = P( (quotedString | unquotedString ).map(s => Config.StringLiteral(s)) )

  val array =
    P( "[" ~/ jsonExpr.rep(sep=",".~/) ~ space ~ "]").map( x => Config.Array(x) )

  val pair = P( string.map(_.value) ~/ space ~
  ((fieldSep   ~/ jsonExpr )
  |(obj ~ space))   )

  val obj: P[Config.Object] =
    P( "{" ~/ objBody ~ "}")

  val objBody = P( pair.rep(sep=("\n"|",").~/) ~ space )
                .map( x => Config.Object(Map(x:_*)) )
                .log()

  val jsonExpr: P[Config.Value] = P(
    space ~ (obj | array | string | `true` | `false` | `null` | number) ~ space
  ).log()

  val root = P( (&(space ~ "{") ~/ obj )|(objBody)   ~ End ).log()

}
