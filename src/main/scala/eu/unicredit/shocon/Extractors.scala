package eu.unicredit.shocon

trait Extractors { 
  type Extractor[T] = PartialFunction[Ast.Value, T]

  implicit val BooleanExtractor: Extractor[Boolean] = {
    case Ast.BooleanLiteral(v) => v 
  } 
  implicit val StringExtractor:  Extractor[String] = {
    case Ast.StringLiteral(v) => v 
  } 
  implicit val DoubleExtractor:  Extractor[Double] = {
    case Ast.NumberLiteral(v) => v.toDouble
  }
  implicit val LongExtractor:    Extractor[Long] = {
    case Ast.NumberLiteral(v) => v.toLong 
  }
  implicit val ObjectExtractor:  Extractor[Ast.Object] = {
    case obj@Ast.Object(_) => obj
  } 
  implicit val GenericExtractor:  Extractor[Ast.Value] = {
    case x:Ast.Value => x
  } 
  implicit def SeqExtractor[T](implicit ex: Extractor[T]): Extractor[Seq[T]] = {
    case Ast.Array(seq) => seq.map(ex.apply(_))
  }
}