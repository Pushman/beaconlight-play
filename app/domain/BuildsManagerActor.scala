package domain

import akka.actor.{ActorRef, Cancellable, Actor}
import concurrent.duration._
import domain.BeaconLightActorCommands.{Stop, Activate}
import BuildsManagerActor._
import BeaconLightManagerCommands._
import BeaconLightManagerProperties._

class BuildsManagerActor(beaconLight: ActorRef, statusReader: StatusReader) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global
  
  var enabled: Boolean = _
  var observedBuilds: Set[BuildIdentifier] = _
  var updateTimeout: Cancellable = _

  override def preStart() {
    enabled = true
    observedBuilds = Set[BuildIdentifier]()
    updateTimeout = context.system.scheduler.schedule(0 milliseconds, updatePeriod, self, CheckStatus)
  }

  override def postStop() {
    updateTimeout.cancel()
  }

  override def receive = {
    case RegisterObservedBuild(build) ⇒
      observedBuilds += build

    case CheckStatus if enabled ⇒
      if (containsUnhandledBrokenBuild(buildStatuses))
        beaconLight ! Activate
      else
        beaconLight ! Stop

    case Enable ⇒ enabled = true
    case Disable ⇒ enabled = false
  }

  private def buildStatuses = for {
    build <- observedBuilds
  } yield statusReader.readBuild(build)
}

object BuildsManagerActor {

  def containsUnhandledBrokenBuild(builds: Set[BuildStatus]) =
    containsBrokenBuild(builds) && !containsBuildInProgress(builds)

  def containsBrokenBuild(builds: Set[BuildStatus]) =
    builds.exists(_.isBroken)

  def containsBuildInProgress(builds: Set[BuildStatus]) =
    builds.exists(_.isInProgress)
}

case class BuildIdentifier(name: String)

object BeaconLightManagerCommands {

  case class RegisterObservedBuild(identifier: BuildIdentifier)

  case object CheckStatus

  case object Disable

  case object Enable

}

object BeaconLightManagerProperties {

  val updatePeriod = (1 minutes)
}