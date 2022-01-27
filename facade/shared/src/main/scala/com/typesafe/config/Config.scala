package com.typesafe.config

import org.akkajs.shocon
import org.akkajs.shocon.Config.Value
import org.akkajs.shocon.Extractor

import java.util.{ concurrent => juc }
import java.{ time => jt, util => ju }
import scala.collection.compat._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.language.experimental.macros

object ConfigFactory {

  import org.akkajs.shocon.ConfigLoader

  def parseString(s: String): Config = macro ConfigLoader.loadFromString

  def load(): Config = macro ConfigLoader.loadDefaultImpl

  def load(cl: ClassLoader): Config = macro ConfigLoader.loadDefaultImplCL

  def defaultReference(): Config = macro ConfigLoader.loadDefaultImpl

  def defaultReference(cl: ClassLoader): Config = macro ConfigLoader.loadDefaultImplCL

  def empty(): Config = Config(shocon.Config.gen("{}"))

  def parseMap(values: java.util.Map[String, Any]): Config =
    Config(shocon.Config.Object.fromPairs(values.asScala.map {
      case (k, v) => k -> shocon.Config.StringLiteral(v.toString)
    }.toSeq))

  def load(conf: Config): Config = conf
}

case class Config(cfg: shocon.Config.Value) { self =>
  import shocon.ConfigOps
  import shocon.Extractors._

  val fallbackStack: mutable.Queue[shocon.Config.Value] = mutable.Queue(cfg)

  def this() = {
    this(shocon.Config.gen("{}"))
  }

  def root(): ConfigObject = {
    new ConfigObject() {
      val inner: Value = self.cfg
      def unwrapped: ju.Map[String, Any] = cfg.as[shocon.Config.Object].get.unwrapped.toMap.asJava
      def entrySet(): ju.Set[ju.Map.Entry[String, ConfigValue]] =
        cfg
          .as[shocon.Config.Object]
          .get
          .fields
          .view
          .mapValues(v =>
            new ConfigValue() {
              override val inner: Value = v
            })
          .toMap
          .asJava
          .entrySet()
    }
  }

  def entrySet(): ju.Set[ju.Map.Entry[String, ConfigValue]] = root().entrySet()

  def checkValid(c: Config, paths: String*): Unit = {}

  def resolve(): Config = this

  def withFallback(c: Config): Config = {
    if (c != null) {
      c.fallbackStack.foreach(fallback => fallbackStack.enqueue(fallback))
    }
    this
  }

  def getOrReturnNull[T](path: String)(implicit ev: Extractor[T]): T = {
    lazy val res: T =
      scala.util
        .Try {
          ev(fallbackStack.find(_.get(path).isDefined).flatMap(_.get(path)).get)
        }
        .toOption
        .getOrElse(null.asInstanceOf[T])

    res
  }

  def hasPath(path: String): Boolean =
    fallbackStack.exists(_.get(path).isDefined)

  def getConfig(path: String): Config = {
    try {
      val configs = fallbackStack.toSeq
        .filter(_.get(path).isDefined)
        .map(_.get(path).get)
        .filter(_ != null)
        .map(Config)
        .filter(_ != null)

      val config = configs.head
      configs.tail.foreach { c =>
        config.withFallback(c)
      }
      config
    } catch {
      case _: Throwable => null.asInstanceOf[Config]
    }
  }

  def getString(path: String): String = getOrReturnNull[String](path)

  def getBoolean(path: String): Boolean = getOrReturnNull[Boolean](path)

  def getInt(path: String): Int = getOrReturnNull[Int](path)

  def getLong(path: String): Long = getOrReturnNull[Long](path)

  def getDouble(path: String): Double = getOrReturnNull[Double](path)

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
   * @param input the string to parse
   * @param pathForException path to include in exceptions
   * @return size in bytes
   * @throws ConfigException if string is invalid
   */
  def parseBytes(input: String, pathForException: String): Long = {
    val s: String = unicodeTrim(input)
    val unitString: String = getUnits(s)
    val numberString: String = unicodeTrim(s.substring(0, s.length() - unitString.length()))

    // this would be caught later anyway, but the error message
    // is more helpful if we check it here.
    if (numberString.isEmpty) {
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
          val resultDecimal: BigDecimal = BigDecimal(unitBytes) * BigDecimal(numberString)
          resultDecimal.toBigInt
        }

      if (result.bitLength < 64) {
        result.longValue
      } else {
        throw ConfigException.BadValue(pathForException)
      }
    } catch {
      case _: NumberFormatException =>
        throw ConfigException.BadValue(pathForException)
    }
  }

  def getStringList(path: String): ju.List[String] =
    getOrReturnNull[ju.List[String]](path) match {
      case null => List[String]().asJava
      case ret  => ret
    }

  def getConfigList(path: String): ju.List[Config] =
    getOrReturnNull[ju.List[shocon.Config.Value]](path) match {
      case null => List[Config]().asJava
      case ret  => ret.asScala.map(Config).asJava
    }

  def getDuration(path: String, unit: TimeUnit): Long = {
    val durationValue = getString(path)
    val nanos = parseDurationAsNanos(durationValue)
    unit.convert(nanos, juc.TimeUnit.NANOSECONDS)
  }

  def getDuration(path: String): jt.Duration = {
    val durationValue = getString(path)
    val nanos = parseDurationAsNanos(durationValue)
    jt.Duration.ofNanos(nanos)
  }

  def parseDurationAsNanos(input: String): Long = {
    import juc.TimeUnit._

    val s: String = unicodeTrim(input)
    val originalUnitString: String = getUnits(s)
    var unitString: String = originalUnitString
    val numberString: String = unicodeTrim(s.substring(0, s.length - unitString.length))

    if (numberString.isEmpty) {
      throw ConfigException.BadValue("No number in duration value '" + input + "'")
    }
    if (unitString.length > 2 && !unitString.endsWith("s")) {
      unitString = unitString + "s"
    }

    val units = unitString match {
      case "" | "ms" | "millis" | "milliseconds" => MILLISECONDS
      case "us" | "micros" | "microseconds"      => MICROSECONDS
      case "d" | "days"                          => DAYS
      case "h" | "hours"                         => HOURS
      case "s" | "seconds"                       => SECONDS
      case "m" | "minutes"                       => MINUTES
      case _ =>
        throw ConfigException.BadValue(
          s"""Could not parse time unit '$originalUnitString' (try ns, us, ms, s, m, h, d)""")
    }

    try {
      // return here
      if (numberString.matches("[0-9]+")) {
        units.toNanos(numberString.toLong)
      } else {
        (numberString.toDouble * units.toNanos(1)).toLong
      }
    } catch {
      case _: NumberFormatException =>
        throw ConfigException.BadValue(s"Could not parse duration number '$numberString'")
    }
  }

  def unicodeTrim(s: String): String = s.trim()

  private def getUnits(s: String): String = {
    var i: Int = s.length - 1
    while (i >= 0) {
      val c: Char = s.charAt(i)
      if (!Character.isLetter(c)) {
        return s.substring(i + 1)
      }
      i -= 1
    }
    s.substring(i + 1)
  }

  private val nanos = Set("ns", "nanos", "nanoseconds")

  @deprecated
  def getMillisDuration(path: String): FiniteDuration = {
    try {
      val res = parseDurationAsNanos(getString(path))

      Duration(res, NANOSECONDS)
    } catch {
      case _: Exception => null
    }
  }

  @deprecated
  def getNanosDuration(path: String): FiniteDuration = {
    val res = getString(path)
    val parts = res.split("[ \t]")
    assert(parts.size == 2 && (nanos contains parts(1)))
    Duration(parts(0).toInt, NANOSECONDS)
  }

}
