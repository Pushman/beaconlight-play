package actors

import concurrent.duration._
import akka.testkit.{TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import BuildsManagerCommands.{Disable, CheckStatus}
import BeaconLightCommands.Stop
import JenkinsStatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses}
import org.mockito.Mockito.{mock, verify}
import org.mockito.BDDMockito.given
import domain.{BuildStatus, Build, BuildIdentifier, BeaconLightStrategy}
import configuration.ActorPathKeys
import support.NamedTestProbe

class BuildsManagerActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildStatusSummary = BuildsStatusSummary(Set(Build(BuildIdentifier("some build"), BuildStatus(false, false))))

  private val beaconLight = NamedTestProbe(ActorPathKeys.beaconLight)
  private val statusReader = NamedTestProbe(ActorPathKeys.statusReader)
  private val beaconLightStrategy = mock(classOf[BeaconLightStrategy])

  "Enabled Builds Manager" when {
    val actor = TestActorRef(new BuildsManagerActor(beaconLightStrategy))
    given(beaconLightStrategy.commandFor(buildStatusSummary)).willReturn(Stop)

    "checking for build status" should {
      actor ! CheckStatus

      "obtain status from JenkinsStatusReaderActor" in {
        statusReader.expectMsg(ReadBuildsStatuses)
        statusReader.reply(buildStatusSummary)
      }
      "ask BeaconLightStrategy for action" in {
        verify(beaconLightStrategy).commandFor(buildStatusSummary)
      }
      "pass this action to BeaconLightActor" in {
        beaconLight.expectMsg(Stop)
      }
    }
  }
  "Disabled Builds Manager" when {
    val actor = TestActorRef(new BuildsManagerActor(beaconLightStrategy))
    actor ! Disable

    "checking for build status" should {
      actor ! CheckStatus

      "not do nothing" in {
        statusReader.expectNoMsg(10 millis)
      }
    }
  }
}