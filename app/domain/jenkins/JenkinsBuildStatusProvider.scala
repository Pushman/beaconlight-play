package domain.jenkins

import domain.{BuildStatus, BuildIdentifier}
import concurrent.Future

trait JenkinsBuildStatusProvider {

  def provideBuildStatus(build: BuildIdentifier): Future[BuildStatus]
}
