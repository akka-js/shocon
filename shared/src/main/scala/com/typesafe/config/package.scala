package com.typesafe

package object config {

  implicit def fromStringToShoconConfig(s: String): eu.unicredit.shocon.Config.Value =
  	eu.unicredit.shocon.Config.StringLiteral(s)

  implicit def fromShoconConfigToString(cv: eu.unicredit.shocon.Config.Value): String =
  	cv.toString

}