package actors

import akka.actor.{ActorRef, Actor}
import akka.pattern.pipe
import concurrent.Future
import akka.util.Timeout
import concurrent.duration._
import domain.{Build, BuildIdentifier}
import jenkins.{ReadStatus, JenkinsBuildActorFactory}
import StatusReaderCommands._
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

trait StatusReaderActor extends Actor {
  this: Actor with JenkinsBuildActorFactory =>

  implicit val timeout = Timeout(5 seconds)

  override def receive = {
    case RegisterObservedBuild(buildIdentifier) ⇒
      context.actorOf(newChildActor(buildIdentifier))

    case ReadBuildsStatuses ⇒
      readBuildsStatuses pipeTo sender
  }

  def readBuildsStatuses =
    Future.sequence(context.children.map(readBuildStatus)).map(builds ⇒ BuildsStatusSummary(builds.toSet))

  private def readBuildStatus(actor: ActorRef) =
    (actor ? ReadStatus).mapTo[Build]
}

object StatusReaderCommands {

  case class RegisterObservedBuild(identifier: BuildIdentifier)

  case object ReadBuildsStatuses

  case class BuildsStatusSummary(builds: Set[Build])

}