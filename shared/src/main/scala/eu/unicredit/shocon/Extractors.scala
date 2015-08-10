package eu.unicredit.shocon

import java.{util => ju}
import scala.collection.JavaConverters._

trait Extractors { 
  
  type Extractor[T] = PartialFunction[Config.Value, T]

  implicit val BooleanExtractor: Extractor[Boolean] = {
    case Config.BooleanLiteral(v) => v 
  } 
  implicit val StringExtractor:  Extractor[String] = {
    case Config.StringLiteral(v) => v 
  } 
  implicit val DoubleExtractor:  Extractor[Double] = {
    case Config.StringLiteral(v) => v.toDouble
  }
  implicit val LongExtractor:    Extractor[Long] = {
    case Config.StringLiteral(v) => v.toLong 
  }  
  implicit val IntExtractor:    Extractor[Int] = {
    case Config.StringLiteral(v) => v.toInt 
  }
  implicit def SeqExtractor[T](implicit ex: Extractor[T]): Extractor[Seq[T]] = {
    case Config.Array(seq) => seq.map(ex.apply(_))
  }
  implicit def juListExtractor[T](implicit ex: Extractor[T]): Extractor[ju.List[T]] = {
    case Config.Array(seq) => seq.map(ex.apply(_)).asJava
  }
  implicit def MapExtractor[T](implicit ex: Extractor[T]): Extractor[Map[String, T]] = {
    case Config.Object(keyValues) => keyValues.map{ case (k,v) => (k, ex.apply(v)) }
  }
  implicit val GenericExtractor:  Extractor[Config.Value] = {
    case x => x
  }
  implicit val ObjectExtractor:  Extractor[Config.Object] = {
    case x : Config.Object => x
  } 

}

object Extractors extends Extractors 