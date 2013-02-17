package domain

import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import domain.StatusReaderCommands._
import concurrent.Future
import domain.JenkinsCommands.ReadBuildStatus
import akka.util.Timeout
import concurrent.duration._

class StatusReaderActor(statusReader: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  var observedBuilds = Set[BuildIdentifier]()
  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case RegisterObservedBuild(build) ⇒
      observedBuilds += build

    case ReadBuildsStatuses ⇒
      Future.sequence(observedBuilds.map(readBuildStatus)).map(BuildsStatusSummary) pipeTo sender
  }

  private def readBuildStatus(build: BuildIdentifier) =
    (statusReader ? ReadBuildStatus(build)).mapTo[BuildStatus]
}

object StatusReaderCommands {

  case class RegisterObservedBuild(identifier: BuildIdentifier)

  case object ReadBuildsStatuses

  case class BuildsStatusSummary(builds: Set[BuildStatus])

}