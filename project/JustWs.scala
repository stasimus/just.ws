import sbt._
import Keys._

object JustWs extends Build {
  val projectSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.scalaloader.ws",
    name := "ws-client",
    version := "0.6.2-FINAL",
    scalaVersion := "2.10.3",
    scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.0"),
    autoScalaLibrary := false,
    compileOrder := CompileOrder.ScalaThenJava,
    libraryDependencies ++= Seq(
      "io.netty" % "netty-all" % "4.0.9.Final",
      "com.typesafe.akka" %% "akka-actor" % "2.2.1" % "compile",
      "com.typesafe.akka" %% "akka-testkit" % "2.2.1" % "test",
      "org.specs2" %% "specs2" % "1.14" % "test"
    )
  )

  lazy val root = Project("ws-client", file("."), settings = projectSettings)
}