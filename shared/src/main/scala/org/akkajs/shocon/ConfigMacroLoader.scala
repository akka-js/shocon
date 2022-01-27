package org.akkajs.shocon

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object ConfigMacroLoader {

  import org.akkajs.shocon.verboseLog

  def setVerboseLogImpl(c: blackbox.Context)(): c.Expr[Unit] = {
    import c.universe._

    verboseLog = true

    c.Expr[Unit](q"{}")
  }

  def parse(c: blackbox.Context)(input: c.Expr[String]): c.Expr[Config.Value] = {
    import c.universe._

    // inspiration from: https://github.com/scala/scala/blob/v2.12.6/src/reflect/scala/reflect/api/StandardLiftables.scala
    // thanks @blaisorblade
    def lift[T: Liftable](value: T): Tree = implicitly[Liftable[T]].apply(value)

    def selectShocon(names: Name*): Tree = names.foldLeft(q"_root_.org.akkajs.shocon": Tree) { Select(_, _) }
    def callPackage(names: Name*)(args: List[Tree]): Apply = Apply(selectShocon(names: _*), args)
    def callConfig(name: Name)(args: List[Tree]): Apply = callPackage(TermName("Config"), name)(args)

    def callApply(str: String)(args: List[Tree]): Apply = callConfig(TermName(str))(args)

    def callPackageConst(names: Name*): Tree = selectShocon(names: _*)
    def callConfigConst(name: Name): Tree = callPackageConst(TermName("Config"), name)

    def callConst(str: String) = callConfigConst(TermName(str))

    implicit def liftConfigValue: Liftable[org.akkajs.shocon.Config.Value] =
      Liftable {
        case v: org.akkajs.shocon.Config.SimpleValue =>
          v match {
            case nl: org.akkajs.shocon.Config.NumberLiteral =>
              callApply("NumberLiteral")(lift(nl.value) :: Nil)
            case sl: org.akkajs.shocon.Config.StringLiteral =>
              callApply("StringLiteral")(lift(sl.value) :: Nil)
            case bl: org.akkajs.shocon.Config.BooleanLiteral =>
              callApply("BooleanLiteral")(lift(bl.value) :: Nil)
            case _ =>
              callConst("NullLiteral")
          }
        case arr: org.akkajs.shocon.Config.Array =>
          callApply("Array") {
            val arrayBody = arr.elements.map(lift(_)).toList

            q"Seq( ..$arrayBody )" :: Nil
          }
        case obj: org.akkajs.shocon.Config.Object =>
          callApply("Object") {
            val mapBody = obj.fields.map {
              case (k, v) => q"($k, $v)"
            }

            q"Map( ..$mapBody )" :: Nil
          }
      }

    def fallback(): c.Expr[Config.Value] = {
      if (verboseLog)
        c.warning(c.enclosingPosition, "[shocon-parser] fallback to runtime parser")

      c.Expr[Config.Value](q"""{
        org.akkajs.shocon.ConfigParser.root.parse($input) match{
          case fastparse.core.Parsed.Success(v,_) => v
          case f: fastparse.core.Parsed.Failure[_, _] => throw new Error(f.msg)
        }
      }""")
    }

    input.tree match {
      case q"""$strLit""" =>
        strLit match {
          case Literal(Constant(str)) =>
            val config = org.akkajs.shocon.Config(str.toString)
            val ast = liftConfigValue(config)

            try {
              c.typecheck(ast)

              if (verboseLog)
                c.info(c.enclosingPosition, "[shocon-parser] optimized at compile time", force = false)

              c.Expr[Config.Value](ast)
            } catch {
              case _: Throwable => fallback()
            }
          case _ => fallback()
        }
    }
  }

}
