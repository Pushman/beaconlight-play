package actors

import concurrent.duration._
import akka.testkit.TestFSMRef
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import BeaconLightCommands.{Stop, Activate}
import org.scalatest.matchers.ShouldMatchers
import CapsLockCommands.{TurnOff, TurnOn}
import BeaconLightStates.{Stopped, Sleeping, Active}
import support.NamedTestProbe
import configuration.ActorPathKeys

class BeaconLightActorTest extends WordSpec with ShouldMatchers {

  val activeTime = (5 milli)
  val sleepingTime = (8 milli)
  val error = (2 milli)

  "Stopped Beacon Light Actor" when {
    "activated" should {
      implicit val system = ActorSystem("test")
      val capsLock = NamedTestProbe(ActorPathKeys.capsLock)
      val actor = TestFSMRef(new BeaconLightActor(activeTime, sleepingTime))

      "turn capsLock on" in {
        actor ! Activate
        capsLock.expectMsg(TurnOn)
        actor.stateName should be(Active)
      }
      "change state to Sleep after timeout" in {
        capsLock.expectNoMsg(activeTime - error)
        capsLock.expectMsg(TurnOff)
        actor.stateName should be(Sleeping)
      }
      "turn on again after timeout" in {
        capsLock.expectNoMsg(sleepingTime - error)
        capsLock.expectMsg(TurnOn)
        actor.stateName should be(Active)
      }
    }
  }
  "Activated Beacon Light Actor" when {
    implicit val system = ActorSystem("test")
    val capsLock = NamedTestProbe(ActorPathKeys.capsLock)
    val actor = TestFSMRef(new BeaconLightActor(activeTime, sleepingTime))
    actor.setState(Active)
    actor.stateName should be(Active)
    capsLock.expectMsg(TurnOn)

    "stopped" should {
      actor ! Stop

      "turn capsLock off" in {
        capsLock.expectMsg(TurnOff)
        capsLock.expectNoMsg(activeTime)
        actor.stateName should be(Stopped)
      }
    }
  }
}