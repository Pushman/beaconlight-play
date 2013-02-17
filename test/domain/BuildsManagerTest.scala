package domain

import concurrent.duration._
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import domain.BuildsManagerCommands.{Disable, CheckStatus}
import domain.BeaconLightActorCommands.{Stop, Activate}
import domain.StatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses}

class BuildsManagerTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val successfulBuild: BuildStatus = BuildStatus(false, false)
  private val failedBuild: BuildStatus = BuildStatus(true, false)
  private val failedBuildInProgress: BuildStatus = BuildStatus(true, true)

  private val beaconLight = TestProbe()
  private val statusReader = TestProbe()

  "Enabled Builds Manager" when {
    val actor = TestActorRef(new BuildsManagerActor(beaconLight.ref, statusReader.ref))

    "checking for build status" should {
      "stop Beacon Light beaconLight after received success build status" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildsStatuses)
        statusReader.reply(BuildsStatusSummary(Set(successfulBuild)))

        beaconLight.expectMsg(Stop)
      }
      "active Beacon Light after received failed build status" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildsStatuses)
        statusReader.reply(BuildsStatusSummary(Set(successfulBuild, failedBuild)))

        beaconLight.expectMsg(Activate)
      }
      "stop Beacon Light beaconLight after failed build in progress" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildsStatuses)
        statusReader.reply(BuildsStatusSummary(Set(failedBuild, failedBuildInProgress)))

        beaconLight.expectMsg(Stop)
      }
    }
  }
  "Disabled Builds Manager" when {
    val actor = TestActorRef(new BuildsManagerActor(beaconLight.ref, statusReader.ref))

    actor ! Disable

    "checking for build status" should {
      actor ! CheckStatus

      "not do nothing" in {
        statusReader.expectNoMsg(10 millis)
      }
    }
  }
}