import scala.sys.process._
import scala.language.postfixOps

import sbtwelcome._

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

val circeVersion = "0.14.1"

lazy val tyriantodo =
  (project in file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings( // Normal settings
      name         := "tyriantodo",
      version      := "0.0.1",
      scalaVersion := "3.1.2",
      organization := "com.purplekingdomgames",
      libraryDependencies ++= Seq(
        "io.indigoengine" %%% "tyrian-io" % "0.5.0",
        "org.scalameta"   %%% "munit"     % "0.7.29" % Test
      ),
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser"
      ).map(_ % circeVersion),
      testFrameworks += new TestFramework("munit.Framework"),
      scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
      scalafixOnCompile := true,
      semanticdbEnabled := true,
      semanticdbVersion := scalafixSemanticdb.revision,
      autoAPIMappings   := true
    )
    .settings( // Launch VSCode when you type `code` in the sbt terminal
      code := {
        val command = Seq("code", ".")
        val run = sys.props("os.name").toLowerCase match {
          case x if x contains "windows" => Seq("cmd", "/C") ++ command
          case _                         => command
        }
        run.!
      }
    )
    .settings( // Welcome message
      logo := "Tyrian TODO (v" + version.value + ")",
      usefulTasks := Seq(
        UsefulTask("a", "fastOptJS", "Rebuild the JS (use during development)"),
        UsefulTask("b", "fullOptJS", "Rebuild the JS and optimise (use in production)"),
        UsefulTask("c", "code", "Launch VSCode")
      ),
      logoColor        := scala.Console.MAGENTA,
      aliasColor       := scala.Console.BLUE,
      commandColor     := scala.Console.CYAN,
      descriptionColor := scala.Console.WHITE
    )

lazy val code =
  taskKey[Unit]("Launch VSCode in the current directory")
