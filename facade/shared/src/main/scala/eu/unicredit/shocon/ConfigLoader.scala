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

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object ConfigLoader {

  var verboseLog = false

  def setVerboseLogImpl(c: Context)(): c.Expr[Unit] = {
    import c.universe._

    verboseLog = true

    c.Expr[Unit](q"{}")
  }

  def setVerboseLog(): Unit = macro setVerboseLogImpl

  /// Loads the content of all config files passed with -Xmacro-settings:
  private def loadExplicitConfigFiles(c: Context): Option[String] =
    // check if config files to be loaded are defined via macro setting -Xmacro-settings:shocon.files=file1.conf;file2.conf
    c.settings.find(_.startsWith("shocon.files="))
      // load these files
      .map( _.split("=") match {
      case Array(_,paths) =>
        val (found,notfound) = paths.split(";").toList
          .map( new java.io.File(_) )
          .partition( _.canRead )

        if(notfound.nonEmpty)
          c.warning(c.enclosingPosition, s"shocon - could not read configuration files: $notfound")

        c.warning(c.enclosingPosition, s"shocon - statically reading configuration from $found")
        found
      case _ => Nil
    })
      // concat these files into a single string
      .map( _.map(scala.io.Source.fromFile(_).getLines.mkString("\n")).mkString("\n\n") )

  def loadDefault(c: Context) = {
    import c.universe._

    val configStr: String =
      // load explicitly defined config files vi -Xmacro-settings:file1.conf;file2.conf;...
      loadExplicitConfigFiles(c)
        // or else load application.conf
        .getOrElse{
          try {
            val confPath = new Object {}.getClass
              .getResource("/")
              .toString + "application.conf"

            c.warning(c.enclosingPosition,
              s"shocon - statically reading configuration from $confPath")

            val stream =
              new Object {}.getClass.getResourceAsStream("/application.conf")

            scala.io.Source.fromInputStream(stream).getLines.mkString("\n")
          } catch {
            case e: Throwable =>
              // we use print instead of c.warning, since multiple warnings at the same c.enclosingPosition seem not to work (?)
              println(c.enclosingPosition, s"WARNING: could not load config file: $e")
              "{}"
          }
        }

    c.Expr[com.typesafe.config.Config](q"""{
        com.typesafe.config.Config(
          eu.unicredit.shocon.Config.gen($configStr)
        )
      }""")
  }


  def loadDefaultImpl(c: Context)() = loadDefault(c)
  def loadDefaultImplCL(c: Context)(cl: c.Expr[ClassLoader]) = loadDefault(c)

  def loadFromString(c: Context)(s: c.Expr[String]) = {
    import c.universe._

    s.tree match {
      case q"""$strLit""" =>
        strLit match {
          case Literal(Constant(str)) =>
            if (verboseLog)
              c.info(c.enclosingPosition, "[shocon-facade] optimized at compile time", false)
            
            c.Expr[com.typesafe.config.Config](q"""{
              com.typesafe.config.Config(
                eu.unicredit.shocon.Config.gen(${str.toString})
              )
            }""")
          case _ =>
            if (verboseLog)
              c.warning(c.enclosingPosition, "[shocon-facade] fallback to runtime parser")

            c.Expr[com.typesafe.config.Config](q"""{
              com.typesafe.config.Config(
                eu.unicredit.shocon.Config($strLit)
              )
            }""")
        }
      // case _ =>
      //   if (verboseLog)
      //     c.warning(c.enclosingPosition, "[shocon-facade] fallback to runtime parser ...")

      //   c.Expr[com.typesafe.config.Config](q"""{
      //     com.typesafe.config.Config(
      //       eu.unicredit.shocon.Config($s)
      //     )
      //   }""")
    }
  }
}
