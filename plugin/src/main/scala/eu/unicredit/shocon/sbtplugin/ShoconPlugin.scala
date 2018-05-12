package eu.unicredit.shocon.sbtplugin

import java.io.{BufferedInputStream, FileInputStream, FileNotFoundException, InputStream}
import java.net.JarURLConnection

import sbt.Keys._
import sbt.{AutoPlugin, Def, _}

object ShoconPlugin extends AutoPlugin {

  type ShoconFilter = Function1[(String,InputStream),Boolean]

  object autoImport {
    val shoconAddLib: SettingKey[Boolean] =
      settingKey[Boolean]("If true, add shocon library to project")

    val shoconLoadFromJars: SettingKey[Boolean] =
      settingKey[Boolean]("If true, load reference.conf files from dependency JARs")

    val shoconFilter: SettingKey[ShoconFilter] =
      settingKey[ShoconFilter]("Filter function applied to each found SHOCON config file")

    val shoconConcatFile: SettingKey[File] =
      settingKey[File]("File to which which all detected configuration files are concatenated")

    val shoconFiles: TaskKey[Seq[(String,InputStream)]] =
      taskKey[Seq[(String,InputStream)]]("List of HOCON configuration files to be included statically at compile time")

    val shoconConcat: TaskKey[File] =
      taskKey[File]("Contains all detected configuration files concatenated")

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    shoconAddLib := true,

    shoconLoadFromJars := true,

    shoconFilter := {_:(String,InputStream) => true},

    shoconConcatFile := (crossTarget in Compile).value / "shocon.conf",

    scalacOptions += "-Xmacro-settings:shocon.files=" + shoconConcatFile.value,

    shoconFiles := loadConfigs(
      shoconLoadFromJars.value,
      (dependencyClasspath in Compile).value,
      (unmanagedResources in Compile).value,
      shoconFilter.value,
      streams.value.log),

    shoconConcat := {
      val log = streams.value.log
      val file = shoconConcatFile.value

      log.debug(s"Assembling SHOCON files for project '${name.value}'")
      val config = shoconFiles.value.map( f => s"# SOURCE ${f._1}}\n" + IO.readStream(f._2) ).mkString("\n\n")

      log.debug(s"SHOCON statically compiled into current project:\n$config\n\n")
      IO.write( file, config )
      file
    }

    // Note: adding the shoconConcat task as dependency to compile does not work under Scala.js
    //       if the ScalaJSPlugin is not declared as a requiredPlugin; however, doing so precludes
    //       using this plugin for both, JVM and JS projects. Hence, shoconConcat must be either
    //       called manually, or be defined as a dependency for compile in each project.
    //  compile in Compile := (compile in Compile).dependsOn(shoconConcat).value
  )


  private def loadConfigs(loadFromJars: Boolean,
                          dependecyClassPath: Classpath,
                          unmanagedResources: Seq[File],
                          fileFilter: ShoconFilter,
                          log: Logger): Seq[(String,InputStream)] =
    ((if(loadFromJars) loadDepReferenceConfigs(dependecyClassPath,log)
    else Nil) ++ loadProjectConfigs(unmanagedResources,log))
    .filter(fileFilter)

  private def loadProjectConfigs(unmanagedResources: Seq[File], log: Logger): Seq[(String,InputStream)] = {
    val files = unmanagedResources
      .filter( f => f.getName == "reference.conf" || f.getName == "application.conf")
      .sorted
      .reverse
      .map( f => (f.getAbsolutePath,fin(f)) )
      log.debug("SHOCON config files found in current project:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def loadDepReferenceConfigs(cp: Classpath, log: Logger): Seq[(String,InputStream)] = {
    val (dirs,jars) = cp.files.partition(_.isDirectory)
    loadJarReferenceConfigs(jars,log) ++ loadDirReferenceConfigs(dirs,log)
  }

  private def loadDirReferenceConfigs(dirs: Seq[File], log: Logger): Seq[(String,InputStream)] = {
    val files = dirs
      .map( _ / "reference.conf" )
      .filter( _.isFile )
      .map( f => (f.getAbsolutePath, fin(f)) )
      log.debug("SHOCON config files found in project dependencies:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def loadJarReferenceConfigs(jars: Seq[File], log: Logger): Seq[(String,InputStream)] = {
    val files = jars
      .map( f => new URL("jar:" + f.toURI + "!/reference.conf").openConnection() )
      .map {
        case c: JarURLConnection => try{
          Some((c.toString,c.getInputStream))
        } catch {
          case _: FileNotFoundException => None
        }
      }
      .collect{
        case Some(in) => in
      }

      log.debug("SHOCON config files found in JAR dependencies:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def fin(file: File): BufferedInputStream = new BufferedInputStream(new FileInputStream(file))

}
