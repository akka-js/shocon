package com.typesafe.config

import scala.concurrent.duration._
import scala.concurrent.{ Promise, Future }
import scala.util.{Try, Success, Failure}
import scala.collection.JavaConverters._
import java.{util => ju}
import java.util.{concurrent => juc}
import java.{time => jt}

import eu.unicredit.shocon

object ConfigFactory {
  def parseString(s: String): Config = {
    new Config(shocon.Config(s))
  }

   def empty() = Config(shocon.Config("{}"))
}


case class Config(cfg: shocon.Config.Value) {
  self =>
  import shocon.ConfigOps
  import shocon.Extractors._

  val fallback = Promise[shocon.Config.Value]

  def this() = {
    this(shocon.Config("{}"))
  }

  def root() = {
    new ConfigObject {
      val inner = self.cfg
      def unwrapped =
        cfg.as[shocon.Config.Object].get.fields.map{
          case (k, v) => (k -> v.toString)
        }.asJava
      def entrySet(): ju.Set[ju.Map.Entry[String,ConfigValue]] =
        cfg.as[shocon.Config.Object].get.fields.map{
          case (k, v) => (k -> new ConfigValue {val inner = v})
          }.asJava.entrySet
    }
  }

  def withFallback(c: Config) = {
    fallback.success(c.cfg)
    this
  }

  def getOrReturnNull[T](path: String)(implicit ev: Extractor[T]): T =
    cfg.get(path)
       .flatMap(_.as[T](ev))
       .getOrElse{
         fallback.future.value match {
            case Some(Success(flbCfg)) =>
              new Config(flbCfg).getOrReturnNull[T](path)(ev)
            case _ =>
              //println("Config Exception cannot extract "+path+" from "+cfg)
              null.asInstanceOf[T] //throw ConfigException.Missing(path)
         }
      }

  def hasPath(path: String)     = cfg.get(path).isDefined

  def getConfig(path: String)   = new Config(getOrReturnNull[shocon.Config.Value](path))

  def getString(path: String)   = getOrReturnNull[String](path)

  def getBoolean(path: String)  = getOrReturnNull[Boolean](path)

  def getInt(path: String)      = getOrReturnNull[Int](path)

  def getDouble(path: String)   = getOrReturnNull[Double](path)

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
    val numberString: String = unicodeTrim(s.substring(0, s.length - unitString.length))

    if (numberString.length == 0) throw new ConfigException.BadValue("No number in duration value '" + input + "'")
    if (unitString.length > 2 && !unitString.endsWith("s")) unitString = unitString + "s"

    val units = unitString match {
      case "" | "ms" | "millis" | "milliseconds" => MILLISECONDS
      case      "us" | "micros" | "microseconds" => MICROSECONDS
      case      "d"  | "days"                    => DAYS
      case      "h"  | "hours"                   => HOURS
      case      "s"  | "seconds"                 => SECONDS
      case      "m"  | "minutes"                 => MINUTES
      case _ => throw new ConfigException.BadValue(
                 "Could not parse time unit '" + originalUnitString + "' (try ns, us, ms, s, m, h, d)")
    }


    try {
      // return here
      if (numberString.matches("[0-9]+")) units.toNanos(numberString.toLong)
      else (numberString.toDouble * units.toNanos(1)).toLong
    } catch {
      case e: NumberFormatException => {
        throw new ConfigException.BadValue("Could not parse duration number '" + numberString + "'")
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
      val res = getString(path)
      val parts = res.split("[ \t]")
      // either parts(1) is empty string (fallback: ms)
      // or it actually is one spelling for "milliseconds"
      assert( parts.size == 1 || (millis contains parts(1)) )
      Duration(parts(0).toInt, MILLISECONDS)
    } catch {
      case err: Exception => null
    }
  }

  def getNanosDuration(path: String) = {
    val res = getString(path)
    val parts = res.split("[ \t]")
    assert( parts.size == 2 && ( nanos contains parts(1) ) )
    Duration(parts(0).toInt, NANOSECONDS)
  }

}
