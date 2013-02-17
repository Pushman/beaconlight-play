package domain.jenkins

import play.api.libs.json.JsValue
import domain.BuildStatus

trait JenkinsJsonStatusParser {

  def parse(json: JsValue): BuildStatus
}

object JenkinsJsonStatusParserImpl extends JenkinsJsonStatusParser {

  override def parse(json: JsValue) =
    BuildStatus(isFailed(json), isInProgress(json))

  private def isFailed(json: JsValue): Boolean = {
    val lastBuildNumber = (json \ "lastBuild" \ "number").as[Int]
    val lastSuccessfulBuildOption = (json \ "lastSuccessfulBuild" \ "number").asOpt[Int]

    lastSuccessfulBuildOption match {
      case Some(lastSuccessfulBuild) => lastSuccessfulBuild != lastBuildNumber
      case None => true
    }
  }

  private def isInProgress(json: JsValue): Boolean =
    (json \ "inQueue").asOpt[Boolean].getOrElse(false)
}