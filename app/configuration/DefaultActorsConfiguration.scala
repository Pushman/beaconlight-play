package configuration

import configuration.ActorPathKeys._
import akka.actor.Props
import actors._
import org.eligosource.eventsourced.core.{Eventsourced, Receiver}
import util.LoggedActor

trait DefaultActorsConfiguration extends ActorsConfiguration with Configurable {

  def createActor(path: ActorPathKey) = actorFactory(path)(actorByPath(path), path.name)

  private implicit val actorRefFactory = configuration.system

  private def actorFactory(path: ActorPathKey)(props: Props, name: String) = path match {
    case `statusReader` => configuration.eventsourcedExtension.processorOf(props, Some(name))
    case _ => configuration.system.actorOf(props, name)
  }

  private def actorByPath(path: ActorPathKey): Props = Props(path match {
    case `statusReader` ⇒ new JenkinsStatusReaderActor with DefaultJenkinsBuildActorFactory
      with Receiver with Eventsourced {

      def id = 1

      def configuration = DefaultActorsConfiguration.this.configuration
    }

    case `buildsManager` ⇒
      new BuildsManagerActor(configuration.beaconLightStrategy)

    case `beaconLight` ⇒
      new BeaconLightActor(configuration.activeTime, configuration.sleepingTime) with LoggedActor

    case `capsLock` ⇒
      new DummyCapsLockActor
  })
}