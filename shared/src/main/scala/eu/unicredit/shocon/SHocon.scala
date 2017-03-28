/* Copyright 2016 UniCredit S.p.A.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package eu.unicredit

import scala.util.Try

package object shocon extends Extractors {

  object Config {
    type Key = String

    sealed trait Value {
      def unwrapped: Any
    }

    case class Array(elements: Seq[Value]) extends Value {
      lazy val unwrapped = elements.map(_.unwrapped)
    }
    case class Object(fields: Map[Key, Value]) extends Value {
      lazy val unwrapped = fields.mapValues(_.unwrapped)
    }


    trait SimpleValue extends Value

    private def unwrapStringAsNumber(value: String): Try[Any] =
      Try {
        value.toInt
      }.recover {
        case _ => value.toLong
      }.recover {
        case _ => value.toDouble
      }

    case class NumberLiteral(value: String) extends SimpleValue {
      lazy val unwrapped = unwrapStringAsNumber(value).get
    }
    case class StringLiteral(value: String) extends SimpleValue {
      lazy val unwrapped = unwrapStringAsNumber(value).getOrElse(value)
    }
    case class BooleanLiteral(value: Boolean) extends SimpleValue {
      lazy val unwrapped = value
    }
    case object NullLiteral extends SimpleValue {
      def unwrapped = null
    }

    import fastparse.core.Parsed
    def parse(input: String) = ConfigParser.root.parse(input)
    def apply(input: String): Config.Value = parse(input) match{
      case Parsed.Success(v,_) => v
      case f: Parsed.Failure[_, _] => throw new Error(f.msg)
    }
    def fromFile(path: String) = apply(io.Source.fromFile(path).mkString)

    object Object {
      def fromPairs(pairs: Seq[(Key, Value)]): Object = {
        val os = pairs.map{ case (k,v) => reparseKey(k,v) }
        os.foldLeft(shocon.Config.Object(Map()))(mergeConfigValues)
      }
      def reparseKey(key: Key, value: Value): Object = {
        val pos = key.indexOf('.')
        if (pos < 0) shocon.Config.Object(Map(key -> value))
        else {
          val k = key.substring(0, pos)
          val rest = key.substring(pos + 1)
          shocon.Config.Object(Map(k -> reparseKey(rest, value)))
        }
      }

      def mergeConfigValues(base: Value, mergeable: Value): Value = {
        if (base == mergeable) base
        else
          (base, mergeable) match {
            case (Object(map1), Object(map2)) =>
              val m1k = map1.keys.toSet
              // all keys in m2 which are not found in m1
              val diff = map2.keys.filterNot(m1k.contains).toSet
              // m is the map that contains both keys from m2 and m1
              // where if a key is in both, their value is merged
              val m = map1.map {
                case (k, v) => k -> mergeConfigValues(v, map2.getOrElse(k, v))
              } ++ map2.filterKeys(diff.contains)
              Object(m)
            case (Array(seq1), Array(seq2)) =>
              Array(seq1 ++ seq2)

            case (v1, v2) => v2 // always the second wins
          }
      }
    }
  }


  implicit class ConfigOps(val tree:  Config.Value) {
    def as[T](implicit ev: Extractor[T]): Option[T] = Option( ev.applyOrElse(tree, null) )
    def apply(key: String): Config.Value = get(key).get
    def get(key: String): Option[Config.Value] = {
      val keys = key.split('.')
      def visit(v:  Config.Value, keys: Seq[String]): Option[Config.Value] = v match {
        case _ if (keys.isEmpty)     => Some(v)
        case o@Config.Object(fields) =>
            if (fields.contains(keys.head))
              visit(fields(keys.head), keys.tail)
            else None
      }
      visit(tree, keys)
    }

    // def getOrElse[T](fallback: => Config.Value)(implicit ev: Extractor[T]): T =
    //   apply(key)(ev).getOrElse(fallback.get(key)(ev))
  }

}
