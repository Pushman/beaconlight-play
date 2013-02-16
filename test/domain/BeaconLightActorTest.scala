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
      "turn capsLock on and change state to Sleep after timeout" in {
        val actor = TestFSMRef(new BeaconLightActor(testActor, activeTime, sleepingTime))

        actor ! Activate
        expectMsg(TurnOn)
        actor.stateName should be(Active)
        
        expectNoMsg(activeTime - error)
        expectMsg(TurnOff)
        actor.stateName should be(Sleeping)

        expectNoMsg(sleepingTime - error)
        expectMsg(TurnOn)
        actor.stateName should be(Active)
      }
    }
  }
  "Activated Beacon Light Actor" when {
    "stopped" should {
      "turn capsLock off" in {
        val actor = TestFSMRef(new BeaconLightActor(testActor, activeTime, sleepingTime))
        actor.setState(Active)
        expectMsg(TurnOn)

        actor ! Stop
        expectMsg(TurnOff)
        expectNoMsg(activeTime - error)
        actor.stateName should be(Stopped)
      }
    }
  }
}