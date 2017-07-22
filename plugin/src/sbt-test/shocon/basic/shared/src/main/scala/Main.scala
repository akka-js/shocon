import com.typesafe.config.ConfigFactory

object Main {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    // check that reference.conf in lib was loaded
    assert( config.getBoolean("lib.loaded") == true )

    // check that reference.conf in this project was loaded after reference.conf in lib
    assert( config.getInt("basic.id") == 42 )

    // check that application.conf was loaded
    assert( config.getBoolean("basic.overriden") == true )
    assert( config.getString("app.name") == "basic" )
  }
}
