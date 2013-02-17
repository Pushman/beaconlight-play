package domain

import akka.actor.{ActorRef, Actor}
import concurrent.duration._
import BuildsManagerCommands._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Future
import BuildsManagerActor._
import domain.BeaconLightActorCommands.{Stop, Activate}
import domain.StatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses}

class BuildsManagerActor(beaconLight: ActorRef, statusReader: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  var enabled: Boolean = true

  override def receive = {
    case CheckStatus if enabled ⇒
      readBuildStatuses.map {
        summary =>
          if (containsUnhandledBrokenBuild(summary.builds))
            beaconLight ! Activate
          else
            beaconLight ! Stop
      }

    case Enable ⇒ enabled = true
    case Disable ⇒ enabled = false
  }

  def readBuildStatuses: Future[BuildsStatusSummary] =
    (statusReader ? ReadBuildsStatuses).mapTo[BuildsStatusSummary]
}

object BuildsManagerActor {

  def containsUnhandledBrokenBuild(builds: Set[Build]) =
    containsBrokenBuild(builds) && !containsBuildInProgress(builds)

  def containsBrokenBuild(builds: Set[Build]) =
    builds.exists(_.status.isFailed)

  def containsBuildInProgress(builds: Set[Build]) =
    builds.exists(_.status.isInProgress)
}

object BuildsManagerCommands {

  case object CheckStatus

  case object Disable

  case object Enable

}