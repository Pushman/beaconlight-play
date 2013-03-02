package configuration

import akka.actor.ActorRef
import ActorPathKeys._

trait ActorsConfiguration {

  def createActor(path: ActorPathKey): ActorRef
}