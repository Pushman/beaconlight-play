package configuration

import configuration.ActorPathKeys._
import akka.actor.Props
import actors.{JenkinsBuildActor, JenkinsBuildActorFactory, JenkinsStatusReaderActor}
import org.eligosource.eventsourced.core.{Eventsourced, Receiver}
import util.LoggedActor
import domain.jenkins.{JenkinsBuildStatusProvider, JenkinsJsonStatusParser, JenkinsServer}
import domain.BuildIdentifier
import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultActorsConfiguration extends ActorsConfiguration {

  def configuration: Configuration

  trait DefaultJenkinsBuildActorFactory extends JenkinsBuildActorFactory {

    def jenkinsServer: JenkinsServer

    def jsonParser: JenkinsJsonStatusParser

    def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) = Props(new JenkinsBuildActor(buildIdentifier) with JenkinsBuildStatusProvider with LoggedActor {
      def provideBuildStatus(build: BuildIdentifier) = jenkinsServer.fetchStatus(build).map(jsonParser.parse)
    })
  }

  trait JenkinsBuildActorFactoryImpl extends DefaultJenkinsBuildActorFactory {

    def jenkinsServer = configuration.jenkinsServer

    def jsonParser = configuration.jenkinsJsonStatusParser
  }

  override def actorByPath(path: ActorPathKey): Props = path match {
    case `statusReader` => Props(new JenkinsStatusReaderActor
      with JenkinsBuildActorFactoryImpl with Receiver with Eventsourced with LoggedActor {
      def id = 1
    })
  }
}