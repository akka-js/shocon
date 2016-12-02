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
package com.typesafe.config

import eu.unicredit.shocon
import java.{util => ju}
import java.util.{concurrent => juc}
import java.{time => jt}
import java.lang.ClassLoader

import eu.unicredit.shocon.Config.Value

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import scala.collection.mutable

object ConfigFactory {
  def parseString(s: String): Config = {
    new Config(shocon.Config(s))
  }

  import scala.language.experimental.macros
  import scala.reflect.macros.blackbox.Context

  def loadDefault(c: Context) = {
    import c.universe._

    val configStr: String =
      try {
        val confPath = new Object {}.getClass
            .getResource("/")
            .toString + "application.conf"

        c.warning(c.enclosingPosition,
                  s"shocon - statically reading configuration from $confPath")

        val stream =
          new Object {}.getClass.getResourceAsStream("/application.conf")

        scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      } catch {
        case e: Throwable =>
          "{}"
      }

    c.Expr[com.typesafe.config.Config](q"""{
        com.typesafe.config.Config(
          eu.unicredit.shocon.Config($configStr)
        )
      }""")
  }

  def loadDefaultImpl(c: Context)() = loadDefault(c)
  def loadDefaultImplCL(c: Context)(cl: c.Expr[ClassLoader]) = loadDefault(c)

  def load(): Config = macro loadDefaultImpl

  def load(cl: ClassLoader): Config = macro loadDefaultImplCL

  def defaultReference(cl: ClassLoader): Config = macro loadDefaultImplCL

  def empty() = Config(shocon.Config("{}"))

  def parseMap(values: java.util.Map[String, Any]) =
    parseString(values.asScala.map{ case (k, v) => s"$k=$v"}.mkString("\n"))

  def load(conf: Config): Config = conf
}

case class Config(cfg: shocon.Config.Value) { self =>
  import shocon.ConfigOps
  import shocon.Extractors._

  val fallbackStack: mutable.Stack[shocon.Config.Value] = mutable.Stack(cfg)

  def this() = {
    this(shocon.Config("{}"))
  }

  def root() = {
    new ConfigObject() {
      val inner = self.cfg
      def unwrapped =
        cfg.as[shocon.Config.Object].get.unwrapped.asJava
      def entrySet(): ju.Set[ju.Map.Entry[String, ConfigValue]] =
        cfg.as[shocon.Config.Object].get.fields.mapValues(v => new ConfigValue() {
          override val inner: Value = v
        }).asJava.entrySet()
//        cfg.as[shocon.Config.Object].get.fields.map {
//            case (k, v) => (k -> new ConfigValue { val inner = v })
//          }.asJava.entrySet
    }
  }

  def entrySet(): ju.Set[ju.Map.Entry[String, ConfigValue]] = root.entrySet()

  def checkValid(c: Config, paths: String*): Unit = {}

  def withFallback(c: Config) = {
    fallbackStack.push(c.cfg)
    this
  }

  def getOrReturnNull[T](path: String)(implicit ev: Extractor[T]): T =
    fallbackStack
      .find(_.get(path).isDefined)
      .flatMap(_.get(path)).orNull

  def hasPath(path: String): Boolean =
    fallbackStack.exists(_.get(path).isDefined)


  def getConfig(path: String) =
    Config(getOrReturnNull[shocon.Config.Value](path))

  def getString(path: String) = getOrReturnNull[String](path)

  def getBoolean(path: String): Boolean = getOrReturnNull[Boolean](path)

  def getInt(path: String) = getOrReturnNull[Int](path)

  def getDouble(path: String) = getOrReturnNull[Double](path)

  def getBytes(path: String): Long = {
    val bytesValue = getString(path)
    parseBytes(bytesValue, path)
  }

  /**
    * Parses a size-in-bytes string. If no units are specified in the string,
    * it is assumed to be in bytes. The returned value is in bytes. The purpose
    * of this function is to implement the size-in-bytes-related methods in the
    * Config interface.
    *
    * @param input
    *            the string to parse
    * @param pathForException
    *            path to include in exceptions
    * @return size in bytes
    * @throws ConfigException
    *             if string is invalid
    */
  def parseBytes(input: String, pathForException: String): Long = {
    val s: String = unicodeTrim(input)
    val unitString: String = getUnits(s)
    val numberString: String = unicodeTrim(
      s.substring(0, s.length() - unitString.length()))

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.length() == 0) {
      throw ConfigException.BadValue(pathForException)
    }
    val units: Option[MemoryUnit] = MemoryUnit.parseUnit(unitString)

    if (units.isEmpty) {
      throw ConfigException.BadValue(pathForException)
    }

    try {
      val unitBytes = units.get.bytes
      val result: BigInt =
        // if the string is purely digits, parse as an integer to avoid
        // possible precision loss; otherwise as a double.
        if (numberString.matches("[0-9]+")) {
          unitBytes * BigInt(numberString)
        } else {
          val resultDecimal: BigDecimal = BigDecimal(unitBytes) * BigDecimal(
              numberString)
          resultDecimal.toBigInt()
        }

      if (result.bitLength < 64) {
        result.longValue()
      } else {
        throw ConfigException.BadValue(pathForException)
      }
    } catch {
      case e: NumberFormatException =>
        throw ConfigException.BadValue(pathForException)
    }
  }

  def getStringList(path: String): ju.List[String] =
    getOrReturnNull[ju.List[String]](path) match {
      case null => List[String]().asJava
      case ret => ret
    }

  def getDuration(path: String, unit: TimeUnit): Long = {
    val durationValue = getString(path)
    val nanos = parseDurationAsNanos(durationValue)
    unit.convert(nanos, juc.TimeUnit.NANOSECONDS)
  }

  def getDuration(path: String): jt.Duration = {
    val durationValue = getString(path)
    val nanos = parseDurationAsNanos(durationValue)
    return jt.Duration.ofNanos(nanos)
  }

  def parseDurationAsNanos(input: String): Long = {
    import juc.TimeUnit._

    val s: String = unicodeTrim(input)
    val originalUnitString: String = getUnits(s)
    var unitString: String = originalUnitString
    val numberString: String = unicodeTrim(
      s.substring(0, s.length - unitString.length))

    if (numberString.length == 0)
      throw new ConfigException.BadValue(
        "No number in duration value '" + input + "'")
    if (unitString.length > 2 && !unitString.endsWith("s"))
      unitString = unitString + "s"

    val units = unitString match {
      case "" | "ms" | "millis" | "milliseconds" => MILLISECONDS
      case "us" | "micros" | "microseconds" => MICROSECONDS
      case "d" | "days" => DAYS
      case "h" | "hours" => HOURS
      case "s" | "seconds" => SECONDS
      case "m" | "minutes" => MINUTES
      case _ =>
        throw new ConfigException.BadValue(
          "Could not parse time unit '" + originalUnitString + "' (try ns, us, ms, s, m, h, d)")
    }

    try {
      // return here
      if (numberString.matches("[0-9]+")) units.toNanos(numberString.toLong)
      else (numberString.toDouble * units.toNanos(1)).toLong
    } catch {
      case e: NumberFormatException => {
        throw new ConfigException.BadValue(
          "Could not parse duration number '" + numberString + "'")
      }
    }
  }

  def unicodeTrim(s: String) = s.trim()

  private def getUnits(s: String): String = {
    var i: Int = s.length - 1
    while (i >= 0) {
      val c: Char = s.charAt(i)
      if (!Character.isLetter(c)) return s.substring(i + 1)
      i -= 1
    }
    return s.substring(i + 1)
  }

  private val millis = Set("ms", "millis", "milliseconds")
  private val nanos = Set("ns", "nanos", "nanoseconds")
  def getMillisDuration(path: String) = {
    try {
      val res = parseDurationAsNanos(getString(path))

      Duration(res, NANOSECONDS)
    } catch {
      case err: Exception => null
    }
  }

  def getNanosDuration(path: String) = {
    val res = getString(path)
    val parts = res.split("[ \t]")
    assert(parts.size == 2 && (nanos contains parts(1)))
    Duration(parts(0).toInt, NANOSECONDS)
  }

}
