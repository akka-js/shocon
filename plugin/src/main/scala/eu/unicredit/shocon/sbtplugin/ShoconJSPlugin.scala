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
package eu.unicredit.shocon.sbtplugin

import java.io.{BufferedInputStream, FileInputStream, FileNotFoundException, InputStream}
import java.net.JarURLConnection

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.impl.DependencyBuilders
import sbt.Keys._
import sbt.{AutoPlugin, Def, _}

object ShoconJSPlugin extends AutoPlugin {

  type ShoconFilter = Function1[(String,InputStream),Boolean]
  override def requires: Plugins = ScalaJSPlugin

  object autoImport {
    val shoconAddLib: SettingKey[Boolean] =
      settingKey[Boolean]("If true, add shocon library to project")

    val shoconDebug: SettingKey[Boolean] =
      settingKey[Boolean]("If true, print debug about the assembled SHOCON file")

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

    shoconDebug := false,

    shoconLoadFromJars := true,

    shoconFilter := {_:(String,InputStream) => true},

    shoconConcatFile := (crossTarget in Compile).value / "shocon.conf",

    scalacOptions += "-Xmacro-settings:shocon.files=" + shoconConcatFile.value,

    shoconFiles := loadConfigs(
      shoconLoadFromJars.value,
      (dependencyClasspath in Compile).value,
      (unmanagedResources in Compile).value,
      shoconFilter.value,
      shoconDebug.value),

    shoconConcat := {
      val log = streams.value.log
      val file = shoconConcatFile.value

      if(shoconDebug.value)
        log.debug(s"Assembling SHOCON files for project '${name.value}'")
      val config = shoconFiles.value.map( f => s"# SOURCE ${f._1}}\n" + IO.readStream(f._2) ).mkString("\n\n")

      if(shoconDebug.value)
        log.debug(s"SHOCON statically compiled into current project:\n$config\n\n")
      IO.write( file, config )
      file
    },

    compile in Compile <<= (compile in Compile).dependsOn(shoconConcat),

    libraryDependencies ++= {
      if (shoconAddLib.value)
        Seq("shocon") map { dep =>
          DepBuilder.toScalaJSGroupID("eu.unicredit") %%% dep % Version.shoconVersion
        }
      else Nil
    }

  )


  private def loadConfigs(loadFromJars: Boolean,
                          dependecyClassPath: Classpath,
                          unmanagedResources: Seq[File],
                          fileFilter: ShoconFilter,
                          debug: Boolean): Seq[(String,InputStream)] =
    ((if(loadFromJars) loadDepReferenceConfigs(dependecyClassPath,debug)
    else Nil) ++ loadProjectConfigs(unmanagedResources,debug))
    .filter(fileFilter)

  private def loadProjectConfigs(unmanagedResources: Seq[File], debug: Boolean): Seq[(String,InputStream)] = {
    val files = unmanagedResources
      .filter( f => f.getName == "reference.conf" || f.getName == "application.conf")
      .sorted
      .reverse
      .map( f => (f.getAbsolutePath,fin(f)) )
    if(debug)
      println("SHOCON config files found in current project:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def loadDepReferenceConfigs(cp: Classpath, debug: Boolean): Seq[(String,InputStream)] = {
    val (dirs,jars) = cp.files.partition(_.isDirectory)
    loadJarReferenceConfigs(jars,debug) ++ loadDirReferenceConfigs(dirs,debug)
  }

  private def loadDirReferenceConfigs(dirs: Seq[File], debug: Boolean): Seq[(String,InputStream)] = {
    val files = dirs
      .map( _ / "reference.conf" )
      .filter( _.isFile )
      .map( f => (f.getAbsolutePath, fin(f)) )
    if(debug)
      println("SHOCON config files found in project dependencies:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def loadJarReferenceConfigs(jars: Seq[File], debug: Boolean): Seq[(String,InputStream)] = {
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

    if(debug)
      println("SHOCON config files found in JAR dependencies:\n" + files.map( "    "+_._1).mkString("","\n","\n\n"))
    files
  }

  private def fin(file: File): BufferedInputStream = new BufferedInputStream(new FileInputStream(file))

  private object DepBuilder extends DependencyBuilders
}
