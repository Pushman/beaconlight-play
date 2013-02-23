package domain.jenkins

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json.{JsValue, Json}
import domain.BuildStatus

class JenkinsJsonStatusParserTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val successJson = Json.parse( """{
    "lastBuild" :           { "number" : 81 },
    "lastSuccessfulBuild" : { "number" : 81 },
    "lastCompletedBuild" :  { "number" : 81 }
   }""")
  private val failedJson = Json.parse( """{
    "lastBuild" :           { "number" : 81 },
    "lastSuccessfulBuild" : { "number" : 79 },
    "lastCompletedBuild" :  { "number" : 81 }
   }""")
  private val failedJsonWithoutAnySuccessfulBuilds = Json.parse( """{
    "lastBuild" :           { "number" : 81 },
    "lastCompletedBuild" :  { "number" : 81 }
   }""")
  private val inQueueJson = Json.parse( """{
    "lastBuild" :           { "number" : 81 },
    "lastSuccessfulBuild" : { "number" : 81 },
    "lastCompletedBuild" :  { "number" : 81 },
    "inQueue" :             true
   }""")
  private val inTheFutureJson = Json.parse( """{
    "lastBuild" :           { "number" : 81 },
    "lastSuccessfulBuild" : { "number" : 80 },
    "lastCompletedBuild" :  { "number" : 80 }
   }""")

  private val successBuild: BuildStatus = BuildStatus(isFailed = false, isInProgress = false)
  private val failedBuild: BuildStatus = BuildStatus(isFailed = true, isInProgress = false)
  private val inProgressBuild: BuildStatus = BuildStatus(isFailed = false, isInProgress = true)

  "Jenkins Json Status Parser" when {
    test("successful", successJson, "successful", successBuild)
    test("failed", failedJson, "failed", failedBuild)
    test("failed without any successful builds", failedJsonWithoutAnySuccessfulBuilds, "failed", failedBuild)
    test("in progress", inQueueJson, "in progress", inProgressBuild)
    test("in the future", inTheFutureJson, "in progress", inProgressBuild)

    def test(description: String, givenJson: JsValue, expectedStatusDescription: String, expectedStatus: BuildStatus) {
      s"passed $description build json" should {
        val status = JenkinsJsonStatusParserImpl.parse(givenJson)

        s"return $expectedStatusDescription build status" in {
          status should be(expectedStatus)
        }
      }
    }
  }
}