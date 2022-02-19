package org.akkajs.shocon

import java.{ util => ju }
import scala.jdk.CollectionConverters._

case class Extractor[T](pf: PartialFunction[Config.Value, T], serial: Int) {
  def apply(c: Config.Value): T = pf.apply(c)
  def applyOrElse(c: Config.Value, fallback: PartialFunction[Config.Value, T]): T = pf.applyOrElse(c, fallback)
}

trait Extractors {

  implicit val BooleanExtractor: Extractor[Boolean] = Extractor(
    {
      case Config.StringLiteral(v) =>
        v.trim match {
          case "true" | "on" | "yes"  => true
          case "false" | "off" | "no" => false
          case _                      => throw new IllegalArgumentException(s"Cannot convert '$v' to boolean")
        }
    },
    1)

  implicit val stringExtractor: Extractor[String] = Extractor({
    case Config.StringLiteral(v) => v
  }, 2)

  implicit val doubleExtractor: Extractor[Double] = Extractor({
    case Config.StringLiteral(v) => v.toDouble
  }, 3)

  implicit val longExtractor: Extractor[Long] = Extractor({
    case Config.StringLiteral(v) => v.toLong
  }, 4)

  implicit val intExtractor: Extractor[Int] = Extractor({
    case Config.StringLiteral(v) => v.toInt
  }, 5)

  implicit def seqExtractor[T](implicit ex: Extractor[T]): Extractor[Seq[T]] =
    Extractor({
      case Config.Array(seq) => seq.map(ex.apply)
    }, 6)

  implicit def juListExtractor[T](implicit ex: Extractor[T]): Extractor[ju.List[T]] =
    Extractor({
      case Config.Array(seq) => seq.map(ex.apply).asJava
    }, 7)

  implicit def mapExtractor[T](implicit ex: Extractor[T]): Extractor[Map[String, T]] =
    Extractor({
      case Config.Object(keyValues) => keyValues.map { case (k, v) => (k, ex.apply(v)) }
    }, 8)

  implicit val genericExtractor: Extractor[Config.Value] = Extractor({
    case x => x
  }, 9)

  implicit val objectExtractor: Extractor[Config.Object] = Extractor({
    case x: Config.Object => x
  }, 10)

}

object Extractors extends Extractors
