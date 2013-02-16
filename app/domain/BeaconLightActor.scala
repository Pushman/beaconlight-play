package domain

import akka.actor.{ActorRef, FSM, Actor}
import concurrent.duration._
import BeaconLightActorStates._
import BeaconLightActorCommands._
import domain.CapsLockActorCommands.{TurnOff, TurnOn}

class BeaconLightActor(capsLock: ActorRef, activeTime: FiniteDuration, sleepingTime: FiniteDuration) 
  extends Actor with FSM[State, Null] {

  startWith(Stopped, null)

  when(Stopped) {
    case Event(Activate, _) ⇒
      goto(Active)
  }

  when(Active, stateTimeout = activeTime) {
    case Event(StateTimeout, _) ⇒
      goto(Sleeping)
  }

  when(Sleeping, stateTimeout = sleepingTime) {
    case Event(StateTimeout, _) ⇒
      goto(Active)
  }

  whenUnhandled {
    case Event(Stop, _) ⇒
      goto(Stopped)
  }

  onTransition {
    case _ -> Active ⇒
      capsLock ! TurnOn
    case Active -> _ ⇒
      capsLock ! TurnOff
  }

  initialize
}

object BeaconLightActorStates {

  sealed trait State

  case object Stopped extends State

  case object Active extends State

  case object Sleeping extends State

}

object BeaconLightActorCommands {

  case object Activate

  case object Stop
  
  case object Sleep

}
