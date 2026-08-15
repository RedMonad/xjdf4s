// xjdf4s — XJDF 2.2 domain model for Scala 3
//
// sbt 2.x build definition (Scala 3 syntax, see ./reference/sbt/docs).
// No plugins are required for this build.

val scala3Version          = "3.8.4"
val catsVersion            = "2.13.0"
val munitVersion           = "1.3.0"
val munitScalacheckVersion = "1.3.0"

ThisBuild / organization := "io.github.redmonad"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := scala3Version

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wunused:all",
  "-Wvalue-discard",
  "-Wnonunit-statement"
)

// ---------------------------------------------------------------------------
// xjdf4s-core — the domain model: primitives, model, resources, intents, dsl
// ---------------------------------------------------------------------------
lazy val core = project
  .in(file("modules/core"))
  .settings(
    name := "xjdf4s-core",
    libraryDependencies += "org.typelevel" %% "cats-core" % catsVersion
  )

// ---------------------------------------------------------------------------
// xjdf4s-laws — law checking for the algebraic structures of the model and
// conformance suites for the specification examples (M1.5-3, PR-13: the suite
// references xjdf4s.examples.SpecExamples, hence the `examples` dependency).
// ---------------------------------------------------------------------------
lazy val laws = project
  .in(file("modules/laws"))
  .dependsOn(core, examples)
  .settings(
    name := "xjdf4s-laws",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"            % munitVersion           % Test,
      "org.scalameta" %% "munit-scalacheck" % munitScalacheckVersion % Test,
      // ADR-0009: cats-laws and discipline-munit for potential discipline-based law tests.
      // These are included for resolution verification under Scala 3.8.4.
      // If resolution fails (expected), they remain commented in the ADR reasoning.
      // Uncomment for the resolution check: sbt -batch update
      // "org.typelevel" %% "cats-laws"        % catsVersion           % Test,
      // "org.typelevel" %% "discipline-munit" % "2.0.0"              % Test
    )
  )

// ---------------------------------------------------------------------------
// xjdf4s-examples — runnable examples built from the XJDF specification.
// Demo only (M1.5-3): its conformance suite moved to `laws/SpecExamplesSuite`.
// ---------------------------------------------------------------------------
lazy val examples = project
  .in(file("modules/examples"))
  .dependsOn(core)
  .settings(
    name := "xjdf4s-examples"
  )

lazy val root = project
  .in(file("."))
  .aggregate(core, laws, examples)
  .settings(
    name           := "xjdf4s",
    publish / skip := true
  )
