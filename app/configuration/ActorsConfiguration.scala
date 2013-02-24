package configuration

import akka.actor.Props
import ActorPathKeys._

trait ActorsConfiguration {

  def actorByPath(path: ActorPathKey): Props
}