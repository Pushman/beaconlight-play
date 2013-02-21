package actors

import akka.actor.{ActorRef, FSM, Actor}
import concurrent.duration._
import BeaconLightStates._
import BeaconLightCommands._
import CapsLockCommands.{TurnOff, TurnOn}

class BeaconLightActor(capsLock: ActorRef, activeTime: FiniteDuration, sleepingTime: FiniteDuration) 
  extends Actor with FSM[State, Null] {

  startWith(Stopped, null)

  when(Stopped) {
    case Event(Activate, _) ⇒ {
      goto(Active)
    }
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
    case Stopped -> Active | Sleeping -> Active ⇒ {
      log.debug("TurnOn")
      capsLock ! TurnOn
    }
    case Active -> Stopped | Active -> Sleeping ⇒ {
      log.debug("TurnOff")
      capsLock ! TurnOff
    }
  }

  initialize
}

object BeaconLightStates {

  sealed trait State

  case object Stopped extends State

  case object Active extends State

  case object Sleeping extends State

}

object BeaconLightCommands {

  sealed trait BeaconLightAction
  
  case object Activate extends BeaconLightAction

  case object Stop extends BeaconLightAction
  
  case object Sleep extends BeaconLightAction

}