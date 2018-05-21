package org.akkajs.shocon

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object DirectAccess {

  var i = 0L

  def number(c: Context) = {
    import c.universe._
    i += 1
    c.Expr[String](q"${i.hashCode}")
  }

  def extractString(c: Context)(str: c.Expr[String]): String = {
    import c.universe._

    str.tree match {
      case q"""$strLit""" =>
        strLit match {
          case Literal(Constant(str)) =>
            str.toString
          case _ =>
            throw new Exception(
              "Please provide a plain string literal")
        }
    }
  }

  def getBooleanImpl(c: Context)(path: c.Expr[String]) = {
    import c.universe._

    val pathStr = extractString(c)(path)

    val alternatives = org.akkajs.ConfigMacroLoader.loaded.map {
      cfg =>
      scala.util.Try {
        cfg.get(pathStr).get
      }.toOption
    }.flatten.distinct

    if (alternatives.size == 1) {
      import org.akkajs.shocon.Extractors._
      val ev = implicitly[Extractor[Boolean]]

      val result = ev(alternatives.head)

      c.Expr[Boolean](q"""
      $result
      """)
    } else {
      c.Expr[Boolean](q"""
        getOrReturnNull[Boolean]($path)
      """)
    }
  }

}
