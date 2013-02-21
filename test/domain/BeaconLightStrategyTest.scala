package domain

import akka.testkit.{TestProbe, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import domain.BeaconLightActorCommands.{Stop, Activate}
import domain.StatusReaderCommands.BuildsStatusSummary

class BeaconLightStrategyTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("some build")
  private val successfulBuild = Build(buildIdentifier, BuildStatus(false, false))
  private val failedBuild = Build(buildIdentifier, BuildStatus(true, false))
  private val failedBuildInProgress = Build(buildIdentifier, BuildStatus(true, true))

  "Beacon Light Strategy" when {
    val beaconLightStrategy = new BeaconLightStrategyImpl

    "getting action for successful builds" should {
      val action = beaconLightStrategy.commandFor(BuildsStatusSummary(Set(successfulBuild)))

      "return Activate action" in {
        action should be(Stop)
      }
    }
    "getting action for build in progress" should {
      val action = beaconLightStrategy.commandFor(BuildsStatusSummary(Set(failedBuild, failedBuildInProgress)))

      "return Activate action" in {
        action should be(Stop)
      }
    }
    "getting action for failed builds" should {
      val action = beaconLightStrategy.commandFor(BuildsStatusSummary(Set(successfulBuild, failedBuild)))

      "return Stop action" in {
        action should be(Activate)
      }
    }
  }
}