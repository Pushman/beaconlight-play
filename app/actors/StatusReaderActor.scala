package actors

import akka.actor.{ActorRef, Actor}
import akka.pattern.{ask, pipe}
import concurrent.Future
import akka.util.Timeout
import concurrent.duration._
import domain.{Build, BuildStatus, BuildIdentifier}
import jenkins.JenkinsCommands
import JenkinsCommands.ReadBuildStatus
import StatusReaderCommands._

class StatusReaderActor(jenkins: ActorRef) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  var observedBuilds = Set[BuildIdentifier]()
  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case RegisterObservedBuild(build) ⇒
      observedBuilds += build

    case ReadBuildsStatuses ⇒
      Future.sequence(observedBuilds.map(readBuildStatus)).map(BuildsStatusSummary) pipeTo sender
  }

  private def readBuildStatus(identifier: BuildIdentifier) =
    (jenkins ? ReadBuildStatus(identifier)).mapTo[BuildStatus].map(status => Build(identifier, status))
}

object StatusReaderCommands {

  case class RegisterObservedBuild(identifier: BuildIdentifier)

  case object ReadBuildsStatuses

  case class BuildsStatusSummary(builds: Set[Build])

}