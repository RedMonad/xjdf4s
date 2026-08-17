val scala3Version = "3.8.4"
val catsVersion = "2.12.0"

scalaVersion := scala3Version
organization := "io.github.redmonad"
version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Werror",
  "-Wunused:all",
  "-Yexplicit-nulls"
)

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.scalameta" %% "munit" % "1.3.5" % Test
)

lazy val core = project
  .in(file("modules/core"))
  .settings(name := "xjdf4s-core")

lazy val model = project
  .in(file("modules/model"))
  .dependsOn(core)
  .settings(name := "xjdf4s-model")

lazy val messaging = project
  .in(file("modules/messaging"))
  .dependsOn(model)
  .settings(name := "xjdf4s-messaging")

lazy val protocol = project
  .in(file("modules/protocol"))
  .dependsOn(model, messaging)
  .settings(name := "xjdf4s-protocol")

lazy val dsl = project
  .in(file("modules/dsl"))
  .dependsOn(model)
  .settings(
    name := "xjdf4s-dsl",
    libraryDependencies += "org.typelevel" %% "cats-free" % catsVersion
  )

lazy val root = rootProject
  .aggregate(core, model, messaging, protocol, dsl)
  .settings(
    name := "xjdf4s",
    publish / skip := true,
  )
