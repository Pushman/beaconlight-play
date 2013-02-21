package actors.jenkins

import akka.testkit.{TestActorRef, TestKit}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import JenkinsCommands.ReadBuildStatus
import org.mockito.Mockito.{mock, when, verify}
import concurrent.Promise
import play.api.libs.json.Json
import domain.{BuildStatus, BuildIdentifier}
import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer}

class JenkinsActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val buildIdentifier = BuildIdentifier("success build")
  private val responseJson = Json.parse( """{
    "lastBuild" : {
      "number" : 81
     }
  }""")
  private val buildStatus = BuildStatus(true, false)

  private val mockedJenkinsServer = mock(classOf[JenkinsServer])
  private val mockedJenkinsJsonStatusParser = mock(classOf[JenkinsJsonStatusParser])

  implicit val sender = testActor

  "Jenkins Actor" when {
    val actor = TestActorRef(new JenkinsActor(mockedJenkinsServer, mockedJenkinsJsonStatusParser))
    when(mockedJenkinsServer.fetchStatus(buildIdentifier)).thenReturn(Promise.successful(responseJson).future)
    when(mockedJenkinsJsonStatusParser.parse(responseJson)).thenReturn(buildStatus)

    "getting successful build status" should {
      actor ! ReadBuildStatus(buildIdentifier)

      "read JSON from jenkins server" in {
        verify(mockedJenkinsServer).fetchStatus(buildIdentifier)
      }
      "parse JSON and return build status" in {
        expectMsg(buildStatus)
      }
    }
  }
}
