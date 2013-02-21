package actors

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import BeaconLightCommands.{BeaconLightAction, Stop, Activate}
import StatusReaderCommands.BuildsStatusSummary
import domain.{BeaconLightStrategyImpl, BuildStatus, Build, BuildIdentifier}

class BeaconLightStrategyTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("some build")
  private val successfulBuild = Build(buildIdentifier, BuildStatus(false, false))
  private val failedBuild = Build(buildIdentifier, BuildStatus(true, false))
  private val failedBuildInProgress = Build(buildIdentifier, BuildStatus(true, true))

  "Beacon Light Strategy" when {
    val beaconLightStrategy = new BeaconLightStrategyImpl

    test("successful builds", Set(successfulBuild), Stop)
    test("build in progress", Set(failedBuild, failedBuildInProgress), Stop)
    test("failed builds", Set(successfulBuild, failedBuild), Activate)

    def test(description: String, givenBuilds: Set[Build], expectedAction: BeaconLightAction) {
      s"getting action for $description" should {
        val action = beaconLightStrategy.commandFor(BuildsStatusSummary(givenBuilds))

        s"return $expectedAction action" in {
          action should be(expectedAction)
        }
      }
    }
  }  
}