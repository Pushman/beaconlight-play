package configuration.actors

import akka.actor.Props
import configuration.actors
import actors.ActorPathKeys._
import configuration.configuration.Configurable
import configuration.actors.ActorPathKeys.ActorPathKey

trait DefaultActorsFactory extends ActorsFactory with DefaultActorsConfiguration {
  this: ActorsFactory with Configurable =>

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