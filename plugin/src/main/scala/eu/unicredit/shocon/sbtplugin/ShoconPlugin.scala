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

import java.io.File

import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt.Keys._
import sbt.{AutoPlugin, Def, _}
import org.scalajs.sbtplugin.impl.DependencyBuilders

object ShoconPlugin extends AutoPlugin {


  override def requires: Plugins = ScalaJSPlugin

  object autoImport {
    val shoconConcatFile: SettingKey[File] =
      settingKey[File]("File to which which all detected configuration files are concatenated")

    val shoconFiles: TaskKey[Seq[File]] =
      taskKey[Seq[File]]("List of HOCON configuration files to be included statically at compile time")

    val shoconConcat: TaskKey[File] =
      taskKey[File]("Contains all detected configuration files concatenated")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    shoconConcatFile := (crossTarget in Compile).value / "shocon.conf",

    scalacOptions += "-Xmacro-settings:shocon.files=" + shoconConcatFile.value,

    shoconFiles := (unmanagedResources in Compile).value
      .filter( f => f.getName == "reference.conf" || f.getName == "application.conf")
      .sorted
      .reverse,

    shoconConcat in Compile := {
      val file = shoconConcatFile.value
      IO.write( file, shoconFiles.value.map(IO.read(_)).mkString("\n\n") )
      file
    },

    compile <<= (compile in Compile).dependsOn(shoconConcat in Compile),

    libraryDependencies ++= Seq("shocon") map { dep =>
      DepBuilder.toScalaJSGroupID("eu.unicredit") %%% dep % Version.shoconVersion
    }

  )

  private object DepBuilder extends DependencyBuilders
}
