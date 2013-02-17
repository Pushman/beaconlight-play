import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "beaconlight-play"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  )

  val akkaVersion = "2.1.0"
  val mockitoVersion = "1.9.0"
  val scalatestVersion = "1.9.1"

  val main = play.Project(appName, appVersion, appDependencies).settings(
    libraryDependencies ++= List(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources(),
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion  % "test" withSources(),
      "org.scalatest" %% "scalatest" % scalatestVersion % "test" withSources(),
      "org.mockito" % "mockito-core" % mockitoVersion % "test" withSources()
    )
  )
}