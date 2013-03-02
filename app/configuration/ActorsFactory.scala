package configuration

import configuration.ActorPathKeys.ActorPathKey
import akka.actor.ActorRef

trait ActorsFactory {

  def createActor(path: ActorPathKey): ActorRef
}
