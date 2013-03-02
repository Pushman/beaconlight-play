package configuration

import actors.{JenkinsBuildActor, JenkinsBuildActorFactory}
import domain.BuildIdentifier
import akka.actor.Props
import domain.jenkins.JenkinsBuildStatusProvider

trait DefaultJenkinsBuildActorFactory extends JenkinsBuildActorFactory with Configurable {

  def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) =
    Props(new JenkinsBuildActor(buildIdentifier) with JenkinsBuildStatusProvider {

      def provideBuildStatus(build: BuildIdentifier) =
        configuration.jenkinsServer.fetchStatus(build).map(configuration.jenkinsJsonStatusParser.parse)
    })
}