package actors

import akka.actor.Actor

class CapsLockActor extends Actor {
  def receive = ???
}

object CapsLockCommands {

  case object TurnOn

  case object TurnOff

  case object Toggle

}
