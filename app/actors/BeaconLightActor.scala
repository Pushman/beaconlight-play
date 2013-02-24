package actors

import akka.actor.{FSM, Actor}
import concurrent.duration._
import BeaconLightStates._
import BeaconLightCommands._
import CapsLockCommands.{TurnOff, TurnOn}
import configuration.ActorPathKeys

class BeaconLightActor(activeTime: FiniteDuration, sleepingTime: FiniteDuration)
  extends Actor with FSM[State, Null] {

  val capsLock = context.actorFor(ActorPathKeys.capsLock.path)

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
    case _ -> Active ⇒
      capsLock ! TurnOn

    case Active -> _ ⇒
      capsLock ! TurnOff
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