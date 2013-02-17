package domain.jenkins

import play.api.libs.json.JsValue
import domain.BuildStatus

trait JenkinsJsonStatusParser {

  def parse(json: JsValue): BuildStatus
}
