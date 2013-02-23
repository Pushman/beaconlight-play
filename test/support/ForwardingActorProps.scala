package support

import akka.testkit.TestProbe
import akka.actor.{Actor, Props}

object ForwardingActorProps {

  def apply(testProbe: TestProbe) = Props(new Actor {
    override def receive = {
      case msg => {
        testProbe.ref.tell(msg, sender)
      }
    }
  })
}