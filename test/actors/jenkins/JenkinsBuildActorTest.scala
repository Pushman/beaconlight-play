package actors.jenkins

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import akka.testkit.{TestKit, TestActorRef}
import domain.{Build, BuildStatus, BuildIdentifier}
import akka.actor.ActorSystem
import domain.jenkins.JenkinsBuildStatusProvider
import concurrent.Promise
import util.LoggedActor

class JenkinsBuildActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("test-build")
  private val status: BuildStatus = BuildStatus(false, false)

  trait MockedJenkinsBuildStatusProvider extends JenkinsBuildStatusProvider {
    def provideBuildStatus(build: BuildIdentifier) = Promise.successful(status).future
  }

  implicit val sender = testActor

  "Jenkins Build Actor" when {
    val actor = TestActorRef(new JenkinsBuildActor(buildIdentifier) with MockedJenkinsBuildStatusProvider with LoggedActor)

    "getting its status" should {
      actor ! ReadStatus

      "status should be returned" in {
        expectMsg(Build(buildIdentifier, status))
      }
    }
  }
}