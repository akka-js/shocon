package org.akkajs

import fastparse.Parsed

import scala.annotation.tailrec
import scala.collection.compat._
import scala.language.experimental.macros
import scala.util.{ Try, Using }

package object shocon extends Extractors {

  var verboseLog = false

  def setVerboseLog(): Unit = macro ConfigMacroLoader.setVerboseLogImpl

  object Config {
    type Key = String

    sealed trait Value {
      def unwrapped: Any
    }

    case class Array(elements: Seq[Value]) extends Value {
      lazy val unwrapped: Seq[Any] = elements.map(_.unwrapped)
    }
    case class Object(fields: Map[Key, Value]) extends Value {
      lazy val unwrapped: Seq[(Key, Any)] = fields.view.mapValues(_.unwrapped).to(Seq)
    }

    sealed trait SimpleValue extends Value

    private def unwrapStringAsNumber(value: String): Try[Any] =
      Try {
        value.toInt
      }.recover {
          case _ => value.toLong
        }
        .recover {
          case _ => value.toDouble
        }

    case class NumberLiteral(value: String) extends SimpleValue {
      lazy val unwrapped: Any = unwrapStringAsNumber(value).get
    }
    case class StringLiteral(value: String) extends SimpleValue {
      lazy val unwrapped: Any =
        Try(this.as[Boolean].get).orElse(unwrapStringAsNumber(value)).getOrElse(value)
    }
    case class BooleanLiteral(value: Boolean) extends SimpleValue {
      lazy val unwrapped: Boolean = value
    }
    case object NullLiteral extends SimpleValue {
      def unwrapped: Null = null
    }

    def gen(input: String): Config.Value = macro ConfigMacroLoader.parse

    /* these methods are here only for retro-compatibility and fallbacks */
    def parse(input: String): Parsed[Object] = ConfigParser.parseString(input)
    def apply(input: String): Config.Value = parse(input) match {
      case Parsed.Success(v, _) => v
      case f: Parsed.Failure    => throw new Error(f.msg)
    }
    def fromFile(path: String): Value = {
      Using.resource(io.Source.fromFile(path)) { bufferedSource =>
        apply(bufferedSource.mkString)
      }
    }

    object Object {

      def fromPairs(pairs: Seq[(Key, Value)]): Object = {
        val os = pairs.map { case (k, v) => reparseKey(k, v) }
        os.foldLeft(shocon.Config.Object(Map()))(mergeConfigs)
      }

      def reparseKey(key: Key, value: Value): Object = {
        val pos = key.indexOf('.')
        if (pos < 0) {
          shocon.Config.Object(Map(key -> value))
        } else {
          val split = key.split('.').reverse
          split.tail.foldLeft(shocon.Config.Object(Map(split.head -> value))) {
            case (acc, elem) => shocon.Config.Object(Map(elem -> acc))
          }
        }
      }

      def mergeValues(base: Value, mergeable: Value): Value = {
        if (base == mergeable) {
          base
        } else {
          (base, mergeable) match {
            case (m1: Object, m2: Object)   => mergeConfigs(m1, m2)
            case (Array(seq1), Array(seq2)) => Array(seq1 ++ seq2)
            case (v1, v2)                   => v2 // always the second wins
          }
        }
      }

      def mergeConfigs(base: Object, mergeable: Object): Object = {
        if (base == mergeable) {
          base
        } else {
          val m1k = base.fields.keys.toSet
          // all keys in m2 which are not found in m1
          val diff = mergeable.fields.keys.filterNot(m1k.contains).toSet
          // m is the map that contains both keys from m2 and m1
          // where if a key is in both, their value is merged

          val m = base.fields.map {
              case (k, v) =>
                mergeable.fields.get(k) match {
                  case Some(v2) => k -> mergeValues(v, v2)
                  case _        => k -> v
                }
            } ++ mergeable.fields.view.filter(e => diff.contains(e._1))
          Object(m)
        }
      }
    }
  }

  implicit class ConfigOps(val tree: Config.Value) {
    def as[T](implicit ev: Extractor[T]): Option[T] = Option(ev.applyOrElse(tree, fallback = null))
    def apply(key: String): Config.Value = get(key).get
    def get(key: String): Option[Config.Value] = {
      val keys = key.split('.')
      @tailrec
      def visit(v: Config.Value, keys: Seq[String]): Option[Config.Value] = v match {
        case _ if keys.isEmpty => Some(v)
        case Config.Object(fields) =>
          if (fields.contains(keys.head)) {
            visit(fields(keys.head), keys.tail)
          } else {
            None
          }
      }
      visit(tree, keys.toIndexedSeq)
    }

    // def getOrElse[T](fallback: => Config.Value)(implicit ev: Extractor[T]): T =
    //   apply(key)(ev).getOrElse(fallback.get(key)(ev))
  }

}
