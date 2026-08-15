// Scalafmt integration for the sbt 2.x build: provides scalafmtCheckAll / scalafmtAll
// (ROADMAP M1.0-1, N-44). The scalafmt binary version itself is pinned in .scalafmt.conf
// (version = "3.11.0") and downloaded dynamically by the plugin.
// Version note (ROADMAP open question #7): 2.6.2 is the latest sbt-scalafmt release and is
// cross-published for sbt 2.x + Scala 3.x; the exact coordinate is confirmed by the first
// CI resolve, with fallback per ROADMAP risk R2.
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
