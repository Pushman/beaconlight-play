package actors

import akka.testkit.{TestProbe, TestActorRef, TestKit}
import akka.actor.ActorSystem
import jenkins.{ReadStatus, JenkinsBuildActorFactory}
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import StatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses, RegisterObservedBuild}
import domain.{Build, BuildStatus, BuildIdentifier}
import support.ForwardingActorProps

class StatusReaderActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("success build")
  private val successfulBuild = Build(buildIdentifier, BuildStatus(false, false))
  private val secondBuildIdentifier = BuildIdentifier("failed build")
  private val failedBuild = Build(secondBuildIdentifier, BuildStatus(true, false))

  private val childActor = TestProbe()

  trait MockedJenkinsBuildActorFactory extends JenkinsBuildActorFactory {
    def newChildActor(buildIdentifier: BuildIdentifier) = ForwardingActorProps(childActor)
  }

  implicit val sender = testActor

  "Status Reader with registered builds" when {
    val actor = TestActorRef(new StatusReaderActor with MockedJenkinsBuildActorFactory)

    actor ! RegisterObservedBuild(buildIdentifier)
    actor ! RegisterObservedBuild(secondBuildIdentifier)

    "getting build statuses" should {
      implicit val sender = testActor
      actor ! ReadBuildsStatuses

      "obtain status for each of builds" in {
        childActor.expectMsg(ReadStatus)
        childActor.reply(successfulBuild)
        childActor.expectMsg(ReadStatus)
        childActor.reply(failedBuild)
      }
      "return builds summary" in {
        expectMsg(BuildsStatusSummary(Set(successfulBuild, failedBuild)))
      }
    }
  }
}