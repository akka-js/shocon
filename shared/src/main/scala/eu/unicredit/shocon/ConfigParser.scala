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

  val fieldSep      = P( CharIn(":="))
  val wspace        = P( CharsWhile(Whitespace) )


  val comment = P( "#" ~ CharsWhile(_ != '\n', min = 0) )
  val nlspace = P( (CharsWhile(" \n".toSet, min = 1) | comment ).rep )
  val space = P( (CharsWhile(" ".toSet, min = 1) | comment ).rep )

  val hexDigit      = P( CharIn('0'to'9', 'a'to'f', 'A'to'F') )
  val unicodeEscape = P( "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit )
  val escape        = P( "\\" ~ (CharIn("\"/\\bfnrt") | unicodeEscape) )

  val strChars = P( CharsWhile(StringChars) )
  val quotedString =
    P( nlspace ~ "\"" ~/ (strChars | escape).rep.! ~ "\"")
  val unquotedString =
    P ( nlspace ~ ( (letter | digit | "_" | "-" | ".").rep(min=1).! ).rep(min=1,sep=CharsWhile(_.isSpaceChar)).! )

  val string = P(quotedString|unquotedString).map(Config.StringLiteral)

  val array =
    P( "[" ~/ jsonExpr.rep(sep=",".~/) ~ nlspace ~ "]").map( x => Config.Array(x) )

  val pair = P( string.map(_.value) ~/ space ~
  ((fieldSep   ~/ jsonExpr )
  |(obj ~ space))   )

  val obj: P[Config.Object] =
    P( "{" ~/ objBody ~ "}")

  val objBody = P( pair.rep(sep=(("\n" ~ nlspace ~ ",".?)|(",".~/))) ~ nlspace )
                .map( x => Config.Object(Map(x:_*)) )
                // .log()

  val jsonExpr: P[Config.Value] = P(
    space ~ (obj | array | string) ~ space
  ) // .log()

  val root = P( (&(space ~ "{") ~/ obj )|(objBody)   ~ End ) // .log()

}
