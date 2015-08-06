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

class Config(cfg: shocon.Config.Value) {
  import shocon.ConfigOps
  import shocon.Extractors._

  val fallback = Promise[Config]
  
  def this() = {
    this(shocon.Config("{}"))
  }
  
  def withFallback(c: Config) = {
    fallback.success(c)
    this
  }

  
  def hasPath(path: String) = scala.util.Try { cfg.get[shocon.Config.Value](path) }.isSuccess
  
  def getConfig(path: String) = new Config(cfg.get[shocon.Config.Value](path))
  
  def getString(path: String) =  cfg.get[String](path)
  
  def getBoolean(path: String) = cfg.get[Boolean](path)
  
  def getInt(path: String) = cfg.get[Int](path)
  
  def getDouble(path: String) = cfg.get[Double](path)
    
  def getStringList(path: String) = cfg.get[Seq[String]](path)

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
    assert( nanos contains parts(1) )
    Duration(parts(0).toInt, NANOSECONDS)
  }

}