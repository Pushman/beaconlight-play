package domain.jenkins

import concurrent.Future
import play.api.libs.json.JsValue
import domain.BuildIdentifier

trait JenkinsServer {

  def fetchStatus(build: BuildIdentifier): Future[JsValue]
}