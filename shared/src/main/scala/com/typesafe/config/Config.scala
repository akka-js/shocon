package com.typesafe.config

import scala.concurrent.duration._
import scala.concurrent.{ Promise, Future }
import scala.util.{ Success, Failure }

import eu.unicredit.shocon

object ConfigFactory {
  def parseString(s: String): Config = {
    new Config(shocon.Config(s))
  }
}

abstract class ConfigException extends RuntimeException
object ConfigException {
  case class Missing(path: String) extends ConfigException
}

case class Config(cfg: shocon.Config.Value) {
  import shocon.ConfigOps
  import shocon.Extractors._

  val fallback = Promise[shocon.Config.Value]
  
  def this() = {
    this(shocon.Config("{}"))
  }
  
  def withFallback(c: Config) = {
    fallback.success(c.cfg)
    this
  }


  def getOrThrow[T](path: String)(implicit ev: Extractor[T]) =
    cfg.get(path) 
       .flatMap(_.as[T](ev))
       .getOrElse( throw ConfigException.Missing(path) )
  
  def hasPath(path: String)     = cfg.get(path).isDefined
  
  
  def getConfig(path: String)   = new Config(getOrThrow[shocon.Config.Value](path))
  
  def getString(path: String)   = getOrThrow[String](path)
  
  def getBoolean(path: String)  = getOrThrow[Boolean](path)
  
  def getInt(path: String)      = getOrThrow[Int](path)

  def getDouble(path: String)   = getOrThrow[Double](path)
    
  def getStringList(path: String) = getOrThrow[String](path)

  private val millis = Set("ms", "millis", "milliseconds")
  private val nanos = Set("ns", "nanos", "nanoseconds")
  def getMillisDuration(path: String) = {
    val res = getString(path)
    val parts = res.split("[ \t]")
    // either parts(1) is empty string (fallback: ms) 
    // or it actually is one spelling for "milliseconds" 
    assert( parts.size == 1 || (millis contains parts(1)) )
    Duration(parts(0).toInt, MILLISECONDS)
  }
  
  def getNanosDuration(path: String) = {
    val res = getString(path)
    val parts = res.split("[ \t]")
    assert( parts.size == 2 && ( nanos contains parts(1) ) )
    Duration(parts(0).toInt, NANOSECONDS)
  }

}