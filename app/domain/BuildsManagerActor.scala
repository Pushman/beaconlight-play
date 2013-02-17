package domain

import akka.actor.{ActorRef, Cancellable, Actor}
import concurrent.duration._
import BuildsManagerCommands._
import BuildsManagerProperties._
import domain.JenkinsCommands.ReadBuildStatus
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Future
import BuildsManagerActor._
import domain.BeaconLightActorCommands.{Stop, Activate}

class BuildsManagerActor(beaconLight: ActorRef, statusReader: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  @volatile var enabled: Boolean = _
  var observedBuilds: Set[BuildIdentifier] = _

  override def preStart() {
    enabled = true
    observedBuilds = Set[BuildIdentifier]()
  }

  override def postStop() {
  }

  override def receive = {
    case RegisterObservedBuild(build) ⇒
      observedBuilds += build

    case CheckStatus if isEnabled ⇒ {
      val sequence: Future[Set[BuildStatus]] = Future.sequence(observedBuilds.map(readBuildStatus))
      sequence.map {
        statuses =>
          if (containsUnhandledBrokenBuild(statuses))
            beaconLight ! Activate
          else
            beaconLight ! Stop
      }
    }

    case Enable ⇒ enabled = true
    case Disable ⇒ enabled = false
  }
  
  def isEnabled = {
    enabled
  }

  private def readBuildStatus(build: BuildIdentifier) =
    (statusReader ? ReadBuildStatus(build)).mapTo[BuildStatus]
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

object BuildsManagerCommands {

  case class RegisterObservedBuild(identifier: BuildIdentifier)

  case object CheckStatus

  case object Disable

  case object Enable

}

object BuildsManagerProperties {

  val updatePeriod = (1 minutes)
}

object BuildsManagerState {

  sealed trait State

  case object Idle extends State

  case object WaitingForStatusReadResponse extends State

  case object Disabled extends State

}

object BuildsManagerData {

  sealed trait Data

  case class Observing(observedBuilds: Set[BuildIdentifier]) extends Data

}