package com.typesafe.config

import scala.concurrent.duration._
import scala.concurrent.{ Promise, Future }
import scala.util.{ Success, Failure }
import scala.collection.JavaConverters._
import java.{util => ju}

import eu.unicredit.shocon

object ConfigFactory {
  def parseString(s: String): Config = {
    new Config(shocon.Config(s))
  }

   def empty() = Config(shocon.Config("{}"))
}

abstract class ConfigException extends RuntimeException
object ConfigException {
  case class Missing(path: String) extends ConfigException
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

  def getOrThrow[T](path: String)(implicit ev: Extractor[T]): T =
    cfg.get(path)
       .flatMap(_.as[T](ev))
       .getOrElse{
         fallback.future.value match {
            case Some(Success(flbCfg)) =>
              new Config(flbCfg).getOrThrow[T](path)(ev)
            case _ =>
              //println("Config Exception cannot extract "+path+" from "+cfg)
              null.asInstanceOf[T] //throw ConfigException.Missing(path)
         }
      }

  def hasPath(path: String)     = cfg.get(path).isDefined

  def getConfig(path: String)   = new Config(getOrThrow[shocon.Config.Value](path))

  def getString(path: String)   = getOrThrow[String](path)

  def getBoolean(path: String)  = getOrThrow[Boolean](path)

  def getInt(path: String)      = getOrThrow[Int](path)

  def getDouble(path: String)   = getOrThrow[Double](path)

  def getStringList(path: String): ju.List[String] =
    getOrThrow[ju.List[String]](path) match {
      case null => List[String]().asJava
      case ret => ret
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
