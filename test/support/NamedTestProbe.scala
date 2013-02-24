package support

import akka.testkit.TestProbe
import akka.actor.ActorSystem
import configuration.ActorPathKeys.ActorPathKey

object NamedTestProbe {

  def apply(actorPathKey: ActorPathKey)(implicit system: ActorSystem) = {
    val probe = new TestProbe(system)
    system.actorOf(ForwardingActorProps(probe), actorPathKey.name)
    probe
  }
}