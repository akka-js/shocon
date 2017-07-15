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
package eu.unicredit.shocon

import java.io.InputStream
import java.util.Scanner

import com.typesafe.config.{Config => Cnf}

import scala.reflect.macros.blackbox.Context

object ConfigLoader {

  def load(): Cnf = {
    import collection.JavaConverters._

    val cl = Thread.currentThread().getContextClassLoader
    val config = (cl.getResources("reference.conf").asScala ++ cl.getResources("application.conf").asScala)
      .map(f => convertStreamToString(f.openStream()))
      .mkString("\n")
    Cnf(Config(config))
  }

  private def convertStreamToString(is: InputStream): String =
    new Scanner(is).useDelimiter("\\A") match {
      case s if s.hasNext => s.next
      case _ => ""
    }

  def loadDefault(c: Context) = {
    import c.universe._
    c.Expr(
      q"""{eu.unicredit.shocon.ConfigLoader.load()}"""
    )
  }
  def loadDefaultImpl(c: Context)() = loadDefault(c)
  def loadDefaultImplCL(c: Context)(cl: c.Expr[ClassLoader]) = loadDefault(c)
}
