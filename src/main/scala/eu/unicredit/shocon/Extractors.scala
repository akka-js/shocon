package eu.unicredit.shocon

trait Extractors { 
  
  type Extractor[T] = PartialFunction[Config.Value, T]

  implicit val BooleanExtractor: Extractor[Boolean] = {
    case Config.BooleanLiteral(v) => v 
  } 
  implicit val StringExtractor:  Extractor[String] = {
    case Config.StringLiteral(v) => v 
  } 
  implicit val DoubleExtractor:  Extractor[Double] = {
    case Config.NumberLiteral(v) => v.toDouble
  }
  implicit val LongExtractor:    Extractor[Long] = {
    case Config.NumberLiteral(v) => v.toLong 
  }
  implicit val GenericExtractor:  Extractor[Config.Value] = {
    case x => x
    case obj@Config.Object(_) => obj
  } 
  implicit def SeqExtractor[T](implicit ex: Extractor[T]): Extractor[Seq[T]] = {
    case Config.Array(seq) => seq.map(ex.apply(_))
  }
}