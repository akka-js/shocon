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

    case class NumberLiteral(value: String) extends SimpleValue {
      lazy val unwrapped = Try {
        value.toInt
      }.recover {
        case _ => value.toLong
      }.recover {
        case _ => value.toDouble
      }.get : Any
    }
    case class StringLiteral(value: String) extends SimpleValue {
      val unwrapped = value
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
      case f: Parsed.Failure => throw new Error(f.msg)
    }
    def fromFile(path: String) = apply(io.Source.fromFile(path).mkString)
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
