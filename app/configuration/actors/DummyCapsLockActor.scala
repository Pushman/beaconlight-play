package configuration.actors

import akka.actor.{ActorLogging, Actor}

class DummyCapsLockActor extends Actor with ActorLogging {

  def receive = {
    case msg => {
      log.info(msg.toString)
      //Toolkit.getDefaultToolkit.setLockingKeyState(VK_CAPS_LOCK, true)
    }
  }
}