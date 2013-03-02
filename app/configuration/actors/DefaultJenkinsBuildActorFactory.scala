package configuration.actors

import actors.{JenkinsBuildActor, JenkinsBuildActorFactory}
import domain.BuildIdentifier
import akka.actor.Props
import domain.jenkins.JenkinsBuildStatusProvider
import configuration.configuration.Configurable

trait DefaultJenkinsBuildActorFactory extends JenkinsBuildActorFactory {
  this: JenkinsBuildActorFactory with Configurable =>

  import scala.concurrent.ExecutionContext.Implicits.global

  def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) =
    Props(new JenkinsBuildActor(buildIdentifier) with JenkinsBuildStatusProvider {

      def provideBuildStatus(build: BuildIdentifier) =
        configuration.jenkinsServer.fetchStatus(build).map(configuration.jenkinsJsonStatusParser.parse)
    })
}