package util

import akka.actor.{ActorLogging, Actor}

trait LoggedActor extends Actor with ActorLogging {

  abstract override def receive = {
    case any => {
      log.debug("message: {}", any)
      super.receive(any)
    }
  }

  override def preStart() {
    log.debug("preStart")
    super.preStart()
  }

  override def postStop() {
    log.debug("postStop")
    super.postStop()
  }
}