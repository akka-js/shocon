package eu.unicredit

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

import fastparse.core.Parsed

object ConfigMacroLoader {

  import eu.unicredit.shocon.verboseLog
  
  def setVerboseLogImpl(c: Context)(): c.Expr[Unit] = {
    import c.universe._

    verboseLog = true

    c.Expr[Unit](q"{}")
  }

  def parse(c: Context)(input: c.Expr[String]): c.Expr[shocon.Config.Value] = {
    import c.universe._
    import c.universe.internal.reificationSupport.SyntacticTuple

    def lift[T: Liftable](value: T): Tree = implicitly[Liftable[T]].apply(value)

    def selectShocon(names: Name*) = names.foldLeft(q"_root_.eu.unicredit.shocon": Tree) { Select(_, _) }
    def callPackage(names: Name*)(args: List[Tree])    = Apply(selectShocon(names: _*), args)
    def callConfig(name: Name)(args: List[Tree]) = callPackage(TermName("Config"), name)(args)

    def callApply(str: String)(args: List[Tree]) = callConfig(TermName(str))(args)

    def callPackageConst(names: Name*) = selectShocon(names: _*)
    def callConfigConst(name: Name) =
      callPackageConst(TermName("Config"), name)

    def callConst(str: String) = callConfigConst(TermName(str))

    implicit def liftConfigValue: Liftable[eu.unicredit.shocon.Config.Value] =
      Liftable { cfg =>
        cfg match {
          case v: eu.unicredit.shocon.Config.SimpleValue =>
            v match {
              case nl: eu.unicredit.shocon.Config.NumberLiteral =>
                callApply("NumberLiteral")(lift(nl.value) :: Nil)
              case sl: eu.unicredit.shocon.Config.StringLiteral =>
                callApply("StringLiteral")(lift(sl.value) :: Nil)
              case bl: eu.unicredit.shocon.Config.BooleanLiteral =>
                callApply("BooleanLiteral")(lift(bl.value) :: Nil)
              case _ =>
                callConst("NullLiteral")
            }
          case arr: eu.unicredit.shocon.Config.Array =>
            callApply("Array"){
              val arrayBody = arr.elements.map(lift(_)).toList

              q"Seq( ..$arrayBody )" :: Nil
            }
          case obj: eu.unicredit.shocon.Config.Object =>
            callApply("Object"){
              val mapBody = obj.fields.map{
                case (k, v) => q"($k, $v)"
              }

              q"Map( ..$mapBody )" :: Nil
            }
        }
      }

    def fallback() = {
      if (verboseLog)
        c.warning(c.enclosingPosition, "[shocon-parser] fallback to runtime parser")

      c.Expr[shocon.Config.Value](q"""{
        eu.unicredit.shocon.ConfigParser.root.parse($input) match{
          case fastparse.core.Parsed.Success(v,_) => v
          case f: fastparse.core.Parsed.Failure[_, _] => throw new Error(f.msg)
        }
      }""")
    }

    input.tree match {
      case q"""$strLit""" =>
        strLit match {
          case Literal(Constant(str)) =>

            val config =
              eu.unicredit.shocon.Config(str.toString)

            val ast = liftConfigValue(config)

            try {
              if (verboseLog)
                c.info(c.enclosingPosition, "[shocon-parser] optimized at compile time", false)

              c.typecheck(ast)
              c.Expr[shocon.Config.Value](ast)
            } catch {
              case err: Throwable =>
                fallback()
            }
          case _ =>
            fallback()
        }
    }
  }

}