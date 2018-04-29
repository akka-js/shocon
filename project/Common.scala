import sbt._
import Keys._

object Common extends AutoPlugin {
  override def trigger = allRequirements
  override lazy val buildSettings = Seq(
    name := "shocon",
    organization := "org.akka-js",
    version := "0.3.0-PERF",
    scalaVersion := "2.11.11",
    crossScalaVersions :=
      Vector("2.11.11", "2.12.4"),
    pomExtra := {
      <url>https://github.com/unicredit/shocon</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/unicredit/shocon</connection>
        <developerConnection>scm:git:git@github.com:unicredit/shocon</developerConnection>
        <url>github.com/unicredit/shocon</url>
      </scm>
      <developers>
        <developer>
          <id>evacchi</id>
          <name>Edoardo Vacchi</name>
          <url>https://github.com/evacchi/</url>
        </developer>
        <developer>
          <id>andreaTP</id>
          <name>Andrea Peruffo</name>
          <url>https://github.com/andreaTP/</url>
        </developer>
      </developers>
    },
    publishMavenStyle in ThisBuild := true,
    pomIncludeRepository in ThisBuild := { x =>
      false
    },
    credentials += Credentials(Path.userHome / ".ivy2" / "sonatype.credentials")
  )
}
