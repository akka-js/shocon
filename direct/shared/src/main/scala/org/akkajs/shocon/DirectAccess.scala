package org.akkajs.shocon

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object DirectAccess {

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

    println(showRaw(c.prefix))

    // q"this.cfg" match {
    //   case q"org.akkajs.shocon.Config.Onject(value)" =>
    //     println("Yay")
    //   case _ => println("not sure...")
    // }
// , config: c.Expr[org.akkajs.shocon.Config.Value]

    val pathStr = extractString(c)(path)

    println("on from here...")
    // val res = config.getString(pathStr)

    c.Expr[Boolean](q"""
    true
    """)
  }

}
