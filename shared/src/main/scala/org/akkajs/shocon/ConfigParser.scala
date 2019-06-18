package org.akkajs.shocon

import fastparse._
import NoWhitespace._ // or we can refactor to use an upstream whitespace handler

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
  def letter[_ : P]     = P( lowercase | uppercase )
  def lowercase[_ : P]  = P( CharIn("a-z") )
  def uppercase[_ : P]  = P( CharIn("A-Z") )
  def digit[_ : P]      = P( CharIn("0-9") )

  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val StringChars = NamedFunction(!"\"\\".contains(_: Char), "StringChars")
  val UnquotedStringChars = NamedFunction(!isWhitespaceNoNl(_: Char), "UnquotedStringChars  ")

  def keyValueSeparator[_ : P] = P( CharIn(":="))

  // whitespace
  def comment[_ : P] = P( ("//" | "#") ~ CharsWhile(_ != '\n', 0) )
  def nlspace[_ : P] = P( (CharsWhile(isWhitespace, 1) | comment ).rep )
  def space[_ : P]   = P( ( CharsWhile(isWhitespaceNoNl, 1) | comment ).rep )

  def hexDigit[_ : P] = P( CharIn("0-9", "a-f", "A-F") )
  def unicodeEscape[_ : P]   = P( "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit )
  def escape[_ : P]          = P( "\\" ~ (CharIn("\"/\\bfnrt") | unicodeEscape) )

  // strings
  def strChars[_ : P] = P( CharsWhile(StringChars) )
  def quotedString[_ : P] = P( "\"" ~/ (strChars | escape).rep.! ~ "\"")
  def unquotedString[_ : P] = P ( ( (letter | digit | "_" | "-" | "." | "/").rep(1).! ).rep(1,CharsWhile(_.isSpaceChar)).! )
  def string[_ : P] = P(nlspace) ~ P(quotedString|unquotedString|CharsWhile(_.isSpaceChar).!) // bit of an hack: this would parse whitespace to the end of line
                            .rep(1).map(_.mkString.trim) // so we will trim the remaining right-side
                            .map(Config.StringLiteral)

  // *** Parsing ***
  def array[_: P]: P[Seq[Config.Value]] = P( "[" ~ nlspace ~/ jsonExpr.rep(sep=itemSeparator) ~ nlspace ~ ",".? ~ nlspace ~ "]")

  def repeatedArray[_ :P]: P[Config.Array] =
    array.rep(min = 1, sep=nlspace).map( ( arrays: Seq[Seq[Config.Value]] ) => Config.Array ( arrays.flatten ) )

  def pair[_: P]: P[(String, Config.Value)] = P( string.map(_.value) ~/ space ~
    ((keyValueSeparator   ~/ jsonExpr )
    |(repeatedObj ~ space)) )

  def obj[_: P]: P[Seq[(String, Config.Value)]] = P( "{" ~/ objBody ~ "}")

  def repeatedObj[_: P]: P[Config.Object] =
    obj.rep(min = 1, sep=nlspace).map(fields => Config.Object(Map( fields.flatten :_*) ))

  def itemSeparator[_: P] = P(("\n" ~ nlspace ~ ",".?)|(("," ~ nlspace).?))

  def objBody[_: P] = P( pair.rep(sep=itemSeparator) ~ nlspace ) // .log()

  def jsonExpr[_: P] = P( space ~ (repeatedObj | repeatedArray | string) ~ space ) // .log()

  def root[_: P] = P( (&(space ~ "{") ~/ obj )|(objBody)   ~ End ).map( x => Config.Object.fromPairs(x) ) // .log()

  def parseString(str: String) = parse(str, root(_))

}
