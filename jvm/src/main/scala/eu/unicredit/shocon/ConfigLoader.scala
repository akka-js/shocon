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

import com.typesafe.config.{Config => Cnf}

import scala.reflect.macros.blackbox.Context

object ConfigLoader {


  def loadDefault(c: Context) = {
    import c.universe._

    val configStr: String = {
      try {
        val confPath = new Object {}.getClass
          .getResource("/")
          .toString + "application.conf"

        c.warning(c.enclosingPosition,
          s"shocon - statically reading configuration from $confPath")

        val stream =
          new Object {}.getClass.getResourceAsStream("application.conf")

        scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
      } catch {
        case e: Throwable =>
          println(s"WARNING: could not load config file: $e")
          "{}"
      }
    }

    c.Expr[com.typesafe.config.Config](q"""{
        com.typesafe.config.Config(
          eu.unicredit.shocon.Config($configStr)
        )
      }""")
  }


  def loadDefaultImpl(c: Context)() = loadDefault(c)
  def loadDefaultImplCL(c: Context)(cl: c.Expr[ClassLoader]) = loadDefault(c)
}
