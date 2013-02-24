package configuration

import configuration.ActorPathKeys._
import akka.actor.{ActorLogging, Actor, Props}
import actors._
import org.eligosource.eventsourced.core.{Eventsourced, Receiver}
import util.LoggedActor
import domain.jenkins.JenkinsBuildStatusProvider
import domain.BuildIdentifier
import scala.concurrent.ExecutionContext.Implicits.global

trait DefaultActorsConfiguration extends ActorsConfiguration {

  def configuration: Configuration

  trait DefaultJenkinsBuildStatusProvider extends JenkinsBuildStatusProvider {
    def provideBuildStatus(build: BuildIdentifier) =
      configuration.jenkinsServer.fetchStatus(build).map(configuration.jenkinsJsonStatusParser.parse)
  }

  trait DefaultJenkinsBuildActorFactory extends JenkinsBuildActorFactory {
    def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) =
      Props(new JenkinsBuildActor(buildIdentifier) with DefaultJenkinsBuildStatusProvider with LoggedActor)
  }

  override def actorByPath(path: ActorPathKey): Props = Props(path match {
    case `statusReader` ⇒ new JenkinsStatusReaderActor with DefaultJenkinsBuildActorFactory
      with Receiver with Eventsourced with LoggedActor {
      def id = 1
    }

    case `buildsManager` ⇒
      new BuildsManagerActor(configuration.beaconLightStrategy)

    case `beaconLight` ⇒
      new BeaconLightActor(configuration.activeTime, configuration.sleepingTime)

    case `capsLock` ⇒
      new Actor with ActorLogging {

        def receive = {
          case msg => {
            log.info(msg.toString)
            //Toolkit.getDefaultToolkit.setLockingKeyState(VK_CAPS_LOCK, true)
          }
        }
      }
  })
}