package domain.jenkins

import play.api.libs.json.JsValue
import domain.BuildStatus

trait JenkinsJsonStatusParser {

  def parse(json: JsValue): BuildStatus
}

object JenkinsJsonStatusParserImpl extends JenkinsJsonStatusParser {

  override def parse(json: JsValue) =
    BuildStatus(isFailed(json), isInProgress(json))

  private def isFailed(json: JsValue) =
    lastCompletedBuild(json) != lastSuccessfulBuild(json)

  private def isInProgress(json: JsValue) =
    isInQueue(json) || (lastBuild(json) != lastCompletedBuild(json))

  def isInQueue(json: JsValue) = (json \ "inQueue").asOpt[Boolean].getOrElse(false)

  private def lastBuild(json: JsValue) = (json \ "lastBuild" \ "number").asOpt[Int]

  private def lastSuccessfulBuild(json: JsValue): Option[Int] = (json \ "lastSuccessfulBuild" \ "number").asOpt[Int]

  private def lastCompletedBuild(json: JsValue): Option[Int] = (json \ "lastCompletedBuild" \ "number").asOpt[Int]
}