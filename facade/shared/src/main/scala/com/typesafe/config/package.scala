package com.typesafe

package object config {

  implicit def fromStringToShoconConfig(s: String): org.akkajs.shocon.Config.Value =
  	org.akkajs.shocon.Config.StringLiteral(s)

  implicit def fromShoconConfigToString(cv: org.akkajs.shocon.Config.Value): String =
  	cv.toString

}