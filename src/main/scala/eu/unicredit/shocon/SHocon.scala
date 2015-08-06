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
     zeroOrMore(KeyValue).separatedBy(Separator)  ~> { (x: Seq[Ast.KeyValue]) => Ast.Object( x.map( (kv: Ast.KeyValue) => (kv.key,kv.value) ).toMap ) }
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
    //wscn ~( SimpleValue | Array | Object )
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
  val IdentifierFirstChar = CharPredicate.Alpha ++ '_' ++ '-'
  val IdentifierChar = IdentifierFirstChar ++ CharPredicate.Digit


  def Number: Rule1[Ast.NumberLiteral] = rule {
    capture(oneOrMore(CharPredicate.Digit|'.')) ~> { n => Ast.NumberLiteral(n) }
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

trait Extractor[T] {
  def extract(tree: Ast.Object, key: String): T
}

object SHocon {
  def parse(input: ParserInput) = new ConfigParser(input).InputLine.run()

  implicit object BooleanExtractor extends Extractor[Boolean] {
    def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case Ast.BooleanLiteral(v) => v 
      } 
    }
  }
  implicit object StringExtractor extends Extractor[String] {
    def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case Ast.StringLiteral(v) => v 
      } 
    }
  }
  implicit object DoubleExtractor extends Extractor[Double] {
    def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case Ast.NumberLiteral(v) => v.toDouble
      } 
    }
  }
  implicit object LongExtractor extends Extractor[Long] {
    def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case Ast.NumberLiteral(v) => v.toLong
      } 
    }
  }
  implicit object ObjectExtractor extends Extractor[Ast.Object] {
   def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case obj@Ast.Object(_) => obj
      } 
    } 
  }
  implicit object SeqExtractor extends Extractor[Seq[Long]] {
   def extract(tree: Ast.Object, key: String) = {
      tree.fields(key) match {
        case obj@Ast.Array(seq) => seq.map{ _ match {
          case Ast.NumberLiteral(v) => v.toLong
        }}
      } 
    } 
  }

}

class Config(input: ParserInput) {
  val tree = SHocon.parse(input).get

  def get[T](key: String)(implicit ev: Extractor[T]) = {
    val keys = key.split('.')
    def visit(v: Ast.Object, keys: Seq[String]): T = 
      if (keys.size == 1) ev.extract(v, keys.head) 
      else v match {
        case o@Ast.Object(fields)    => visit(SHocon.ObjectExtractor.extract(o, keys.head), keys.tail)
      }
    visit(tree, keys)
  }

}

