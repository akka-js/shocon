package org.akkajs.shocon

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object ConfigLoader {

  import org.akkajs.shocon.verboseLog

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

        c.info(c.enclosingPosition, s"shocon - statically reading configuration from $found", force=false)
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

            c.info(c.enclosingPosition,
              s"shocon - statically reading configuration from $confPath", force=false)

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
          org.akkajs.shocon.Config.gen($configStr)
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
                org.akkajs.shocon.Config.gen(${str.toString})
              )
            }""")
          case _ =>
            if (verboseLog)
              c.warning(c.enclosingPosition, "[shocon-facade] fallback to runtime parser")

            c.Expr[com.typesafe.config.Config](q"""{
              com.typesafe.config.Config(
                org.akkajs.shocon.Config($strLit)
              )
            }""")
        }
    }
  }
}
