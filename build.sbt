val scala3Version = "3.8.4"
val catsVersion = "2.12.0"
val circeVersion = "0.14.10"

scalaVersion := scala3Version
organization := "io.github.redmonad"
version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Werror",
  "-Wunused:all",
  "-Yexplicit-nulls",
  "-Xmax-inlines",
  "256"
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

lazy val codecXml = project
  .in(file("modules/codec-xml"))
  // "test->test" makes the domain generators (model and messaging test scopes) available to the codec tests,
  // per the sbt multi-project guide on per-configuration classpath dependencies.
  .dependsOn(model % "compile->compile;test->test", messaging % "compile->compile;test->test")
  .settings(name := "xjdf4s-codec-xml")

lazy val codecJson = project
  .in(file("modules/codec-json"))
  .dependsOn(
    model % "compile->compile;test->test",
    messaging % "compile->compile;test->test",
    // compile-time reuse of the XML module's naming policy (Names), defaults lookup and lexical helpers;
    // the same dependency powers the XML<->JSON cross-codec law in the tests
    codecXml % "compile->compile"
  )
  .settings(
    name := "xjdf4s-codec-json",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion % Test
    )
  )

lazy val root = rootProject
  .aggregate(core, model, messaging, protocol, dsl, codecXml, codecJson)
  .settings(
    name := "xjdf4s",
    publish / skip := true,
  )
