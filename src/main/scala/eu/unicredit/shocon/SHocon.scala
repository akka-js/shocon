package eu.unicredit.shocon

import org.parboiled2._


object SHocon extends Extractors {
  def parse(input: ParserInput) = new ConfigParser(input).InputLine.run()

  object Config {
    def apply(input: ParserInput) = new Config(input)
    def fromFile(path: String) = apply(io.Source.fromFile(path).mkString)
  }

  class Config(input: ParserInput) {
    val tree = SHocon.parse(input).get

    def get[T](key: String)(implicit ev: SHocon.Extractor[T]) = {
      val keys = key.split('.')
      def visit(v: Ast.Value, keys: Seq[String]): T = v match {
          case _ if (keys.isEmpty)  => ev.apply(v)
          case o@Ast.Object(fields) => visit(fields(keys.head), keys.tail)
        }
      visit(tree, keys)
    }
  }

}
