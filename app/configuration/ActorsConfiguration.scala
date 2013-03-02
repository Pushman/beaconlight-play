package configuration

import akka.actor.Props
import ActorPathKeys._

trait ActorsConfiguration {

  def actorProps(path: ActorPathKey): Props
}