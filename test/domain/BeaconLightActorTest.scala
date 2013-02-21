package domain

import concurrent.duration._
import akka.testkit.{TestKit, TestFSMRef}
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import domain.BeaconLightActorCommands.{Stop, Activate}
import org.scalatest.matchers.ShouldMatchers
import domain.CapsLockActorCommands.{TurnOff, TurnOn}
import domain.BeaconLightActorStates.{Stopped, Sleeping, Active}

class BeaconLightActorTest extends TestKit(ActorSystem("test")) with WordSpec with ShouldMatchers {

  val activeTime = (500 micro)
  val sleepingTime = (800 micro)
  val error = (100 micro)

  "Stopped Beacon Light Actor" when {
    "activated" should {
      val actor = TestFSMRef(new BeaconLightActor(testActor, activeTime, sleepingTime))

      "turn capsLock on" in {
        actor ! Activate
        expectMsg(TurnOn)
        actor.stateName should be(Active)
      }
      "change state to Sleep after timeout" in {
        expectNoMsg(activeTime - error)
        expectMsg(TurnOff)
        actor.stateName should be(Sleeping)
      }
      "turn on again after timeout" in {
        expectNoMsg(sleepingTime - error)
        expectMsg(TurnOn)
        actor.stateName should be(Active)
      }
    }
  }
  "Activated Beacon Light Actor" when {
    "stopped" should {
      val actor = TestFSMRef(new BeaconLightActor(testActor, activeTime, sleepingTime))
      actor.setState(Active)
      expectMsg(TurnOn)

      "turn capsLock off" in {
        actor ! Stop
        expectMsg(TurnOff)
        expectNoMsg(activeTime - error)
        actor.stateName should be(Stopped)
      }
    }
  }
}