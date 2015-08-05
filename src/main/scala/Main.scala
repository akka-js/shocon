import eu.unicredit.shocon.SHocon
import org.parboiled2._

object Main {
  def main(args: Array[String]): Unit = {


      val input = """
        |foobar [
        |
        |
        |   1 #cmt
        |     // ssss
        |
        |   2 , # SD
        |
        |// prova
        |
        |  {
        |
        |  // altri
        |
        |
        |      x = "test val", //as
        |
        |      # sdsds
        |
        |      // sdddd
        |
        |y = foo
        |
        |      k = 12 //acomment
        |
        | anobj {
        |   k: 123
        |   z = 4444
        |       }
        |
        |      z = 23232
        |      }
        |
        |      {
        |
        |          option = true,
        |
        |          other = truebar
        |
        |      }
        |]
      """.stripMargin.trim()


    print(SHocon.parse(input))

  }
}
