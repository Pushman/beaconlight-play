package domain

import akka.actor.Actor

class CapsLockActor extends Actor {
  def receive = ???
}

object CapsLockActorCommands {

  case object TurnOn

  case object TurnOff

  case object Toggle

}
