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

import scala.reflect.macros.blackbox.Context

object ConfigLoader {

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

    val config =
     eu.unicredit.shocon.Config(configStr)

    import eu.unicredit.shocon.Config
    def flatten(key: Seq[String], cfg: Config.Value): Map[String, String] = {
      cfg match {
        case v: Config.SimpleValue =>
          Map((key).mkString(".") -> v.unwrapped.toString)
        case Config.Array(arr) =>
          Map((key).mkString(".") -> arr.map(_.unwrapped).mkString("[", ", ", "]"))
        case Config.Object(map) =>
          map.flatMap{
            case (k, v) =>
              flatten((key :+ k), v)
          }
      }
    }
    println("DEBUG!")
    println(flatten(Seq(), config))

    val cache = flatten(Seq(), config)

    c.Expr[com.typesafe.config.Config](q"""{
        val res =
        com.typesafe.config.Config(
          () => eu.unicredit.shocon.Config($configStr)
        )
        res.initialCache = scala.collection.mutable.Map(..$cache)
        res
      }""")
  }


  def loadDefaultImpl(c: Context)() = loadDefault(c)
  def loadDefaultImplCL(c: Context)(cl: c.Expr[ClassLoader]) = loadDefault(c)
}
