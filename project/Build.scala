import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName = "beaconlight-play"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq()

  lazy val akkaVersion = "2.1.0"
  lazy val mockitoVersion = "1.9.0"
  lazy val scalatestVersion = "1.9.1"
  lazy val eventsourced = "0.5-SNAPSHOT"
  lazy val `eligosource-snapshots` =
    "Eligosource Snapshots" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-snapshots"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers := Seq(`eligosource-snapshots`),
    libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources(),
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test" withSources(),
      "org.eligosource" %% "eventsourced-core" % eventsourced withSources(),
      "org.eligosource" %% "eventsourced-journal-journalio" % eventsourced withSources(),
      "org.scalatest" %% "scalatest" % scalatestVersion % "test" withSources(),
      "org.mockito" % "mockito-core" % mockitoVersion % "test" withSources()
    )
  )
}