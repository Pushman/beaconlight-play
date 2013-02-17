package domain

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import domain.StatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses, RegisterObservedBuild}
import domain.JenkinsCommands.ReadBuildStatus

class StatusReaderActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("success build")
  private val successfulBuild: BuildStatus = BuildStatus(false, false)
  private val secondBuildIdentifier = BuildIdentifier("failed build")
  private val failedBuild: BuildStatus = BuildStatus(true, false)

  private val statusReader = TestProbe()

  "Status Reader" when {
    val actor = TestActorRef(new StatusReaderActor(statusReader.ref))

    "registering new Build" should {
      actor ! RegisterObservedBuild(buildIdentifier)

      "append it to builds set" in {
        actor.underlyingActor.observedBuilds should (contain(buildIdentifier) and have size (1))
      }
    }
  }
  "Status Reader with registered builds" when {
    val actor = TestActorRef(new StatusReaderActor(statusReader.ref))

    actor ! RegisterObservedBuild(buildIdentifier)
    actor ! RegisterObservedBuild(secondBuildIdentifier)
    
    "getting build statuses" should {
      implicit val sender = testActor
      actor ! ReadBuildsStatuses

      "obtain status for each of builds" in {
        statusReader.expectMsg(ReadBuildStatus(buildIdentifier))
        statusReader.reply(successfulBuild)
        statusReader.expectMsg(ReadBuildStatus(secondBuildIdentifier))
        statusReader.reply(failedBuild)
      }
      "return builds summary" in {
        expectMsg(BuildsStatusSummary(Set(successfulBuild, failedBuild)))
      }
    }
  }
}