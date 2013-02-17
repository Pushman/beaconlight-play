package domain.jenkins

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json.Json
import domain.BuildStatus

class JenkinsJsonStatusParserTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  private val successJson = Json.parse( """{
    "lastBuild" : {
      "number" : 81
     },
     "lastSuccessfulBuild" : {
       "number" : 81
     }
   }""")
  private val failedJson = Json.parse( """{
    "lastBuild" : {
      "number" : 81
     },
    "lastSuccessfulBuild" : {
      "number" : 79
    }
  }""")
  private val failedJsonWithoutAnySuccessfulBuilds= Json.parse( """{
    "lastBuild" : {
      "number" : 81
     },
    "lastSuccessfulBuild" : null
  }""")
  private val failedInProgressJson = Json.parse( """{
    "lastBuild" : {
      "number" : 81
     },
    "lastSuccessfulBuild" : {
      "number" : 79
    },
    "inQueue" : true
  }""")

  "Jenkins Json Status Parser" when {
    "passed successful build json" should {
      "return success build status" in {
        val status = JenkinsJsonStatusParserImpl.parse(successJson)

        status should be(BuildStatus(isFailed = false, isInProgress = false))
      }
    }
    "passed failed build json" should {
      "return failed build status" in {
        val status = JenkinsJsonStatusParserImpl.parse(failedJson)

        status should be(BuildStatus(isFailed = true, isInProgress = false))
      }
    }
    "passed failed without any successful builds json" should {
      "return failed build status" in {
        val status = JenkinsJsonStatusParserImpl.parse(failedJsonWithoutAnySuccessfulBuilds)

        status should be(BuildStatus(isFailed = true, isInProgress = false))
      }
    }
    "passed build in progress json" should {
      "return in progress build status" in {
        val status = JenkinsJsonStatusParserImpl.parse(failedInProgressJson)

        status should be(BuildStatus(isFailed = true, isInProgress = true))
      }
    }
  }
}
