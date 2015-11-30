package eu.unicredit.shocon

import fastparse.all._

object ConfigParser {
  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V){
    def apply(t: T) = f(t)
    override def toString() = name

  }

  val isWhitespace = (c: Char) =>
    c match {
      // try to hit the most common ASCII ones first, then the nonbreaking
      // spaces that Java brokenly leaves out of isWhitespace.
      case ' '|'\n'|'\u00A0'|'\u2007'|'\u202F'|'\uFEFF' /* BOM */ => true;
      case _ => Character.isWhitespace(c);
    }

  val isWhitespaceNoNl = (c: Char) =>  c != '\n' && isWhitespace(c)

  // *** Lexing ***
//  val Whitespace = NamedFunction(isWhitespace, "Whitespace")
  val letter     = P( lowercase | uppercase )
  val lowercase  = P( CharIn('a' to 'z') )
  val uppercase  = P( CharIn('A' to 'Z') )
  val digit      = P( CharIn('0' to '9') )
  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val StringChars = NamedFunction(!"\"\\".contains(_: Char), "StringChars")
  val UnquotedStringChars = NamedFunction(!isWhitespaceNoNl(_: Char), "UnquotedStringChars  ")

  val keyValueSeparator      = P( CharIn(":="))


  // whitespace
//  val wspace        = P( CharsWhile(Whitespace) )
  val comment = P( "#" ~ CharsWhile(_ != '\n', min = 0) )
  val nlspace = P( (CharsWhile(isWhitespace, min = 1) | comment ).rep )
  val space = P( ( CharsWhile(isWhitespaceNoNl, min = 1) | comment ).rep )

  val hexDigit      = P( CharIn('0'to'9', 'a'to'f', 'A'to'F') )
  val unicodeEscape = P( "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit )
  val escape        = P( "\\" ~ (CharIn("\"/\\bfnrt") | unicodeEscape) )

  // strings
  val strChars = P( CharsWhile(StringChars) )
  val quotedString =
    P( nlspace ~ "\"" ~/ (strChars | escape).rep.! ~ "\"")
  val unquotedString =
    P ( nlspace ~ ( (letter | digit | "_" | "-" | ".").rep(min=1).! ).rep(min=1,sep=CharsWhile(_.isSpaceChar)).! )

  val string = P(quotedString|unquotedString).map(Config.StringLiteral)

  // *** Parsing ***

  val array =
    P( "[" ~/ jsonExpr.rep(sep=itemSeparator) ~ nlspace ~ "]").map( x => Config.Array(x) )

  val pair = P( string.map(_.value) ~/ space ~
    ((keyValueSeparator   ~/ jsonExpr )
    |(obj ~ space))   )

  val obj: P[Config.Object] =
    P( "{" ~/ objBody ~ "}")

  val itemSeparator = P(("\n" ~ nlspace ~ ",".?)|(",".~/))

  val objBody = P( pair.rep(sep=itemSeparator) ~ nlspace )
                .map( x => Config.Object(Map(x:_*)) )
                // .log()

  val jsonExpr: P[Config.Value] = P(
    space ~ (obj | array | string) ~ space
  ) // .log()

  val root = P( (&(space ~ "{") ~/ obj )|(objBody)   ~ End ) // .log()

}
