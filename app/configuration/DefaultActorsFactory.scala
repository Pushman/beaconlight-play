package configuration

import akka.actor.Props
import configuration.ActorPathKeys._

trait DefaultActorsFactory extends ActorsFactory with ActorsConfiguration with Configurable {

  def createActor(path: ActorPathKey) =
    actorFactory(path)(actorProps(path), path.name)

  private implicit val actorRefFactory = configuration.system

  private def actorFactory(path: ActorPathKey)(props: Props, name: String) =
    if (isEventSourced(path))
      configuration.eventsourcedExtension.processorOf(props, Some(name))
    else
      configuration.system.actorOf(props, name)

  private def isEventSourced(path: ActorPathKey) =
    path == `statusReader`
}
