package domain

import concurrent.duration._
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import domain.BuildsManagerCommands.{Enable, Disable, CheckStatus, RegisterObservedBuild}
import domain.JenkinsCommands.ReadBuildStatus
import domain.BeaconLightActorCommands.{Stop, Activate}

class BuildsManagerTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("success build")
  private val successfulBuild: BuildStatus = BuildStatus(false, false)
  private val secondBuildIdentifier = BuildIdentifier("failed build")
  private val failedBuild: BuildStatus = BuildStatus(true, false)
  private val failedBuildInProgress: BuildStatus = BuildStatus(true, true)

  "Builds Manager" when {
    "registering new Build" should {
      "append it to builds set" in {
        val actor = TestActorRef(new BuildsManagerActor(testActor, null))

        actor ! RegisterObservedBuild(buildIdentifier)

        actor.underlyingActor.observedBuilds should contain(buildIdentifier)
      }
    }
    "enabled" should {
      val beaconLight = TestProbe()
      val statusReader = TestProbe()
      val actor = TestActorRef(new BuildsManagerActor(beaconLight.ref, statusReader.ref))

      actor ! RegisterObservedBuild(buildIdentifier)
      actor ! RegisterObservedBuild(secondBuildIdentifier)

      "stop Beacon Light beaconLight after received success build status" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildStatus(buildIdentifier))
        statusReader.reply(successfulBuild)
        statusReader.expectMsg(ReadBuildStatus(secondBuildIdentifier))
        statusReader.reply(successfulBuild)

        beaconLight.expectMsg(Stop)
      }
      "active Beacon Light after received failed build status" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildStatus(buildIdentifier))
        statusReader.reply(successfulBuild)
        statusReader.expectMsg(ReadBuildStatus(secondBuildIdentifier))
        statusReader.reply(failedBuild)

        beaconLight.expectMsg(Activate)
      }
      "stop Beacon Light beaconLight after failed build in progress" in {
        actor ! CheckStatus

        statusReader.expectMsg(ReadBuildStatus(buildIdentifier))
        statusReader.reply(failedBuild)
        statusReader.expectMsg(ReadBuildStatus(secondBuildIdentifier))
        statusReader.reply(failedBuildInProgress)

        beaconLight.expectMsg(Stop)
      }
    }
    "disabled" should {
      val beaconLight = TestProbe()
      val statusReader = TestProbe()
      val actor = TestActorRef(new BuildsManagerActor(beaconLight.ref, statusReader.ref))

      actor ! RegisterObservedBuild(buildIdentifier)

      "not check statuses for builds" in {
        actor ! Disable
        actor ! CheckStatus

        actor.underlyingActor.enabled should be(false)
        statusReader.expectNoMsg(10 millis)
      }
      "allow to activate" in {
        actor ! Enable

        actor.underlyingActor.enabled should be(true)
        statusReader.expectNoMsg(10 millis)
      }
    }
  }
}