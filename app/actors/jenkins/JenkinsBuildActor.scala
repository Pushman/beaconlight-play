package actors.jenkins

import akka.actor.Actor
import akka.pattern.pipe
import domain.jenkins.JenkinsBuildStatusProvider
import domain.{Build, BuildIdentifier}
import scala.concurrent.ExecutionContext.Implicits.global

trait JenkinsBuildActor extends Actor {
  this: Actor with JenkinsBuildStatusProvider ⇒

  private var buildIdentifier: BuildIdentifier = _

  override def receive: Receive = {
    case SetBuildIdentifier(build) ⇒
      buildIdentifier = build

    case ReadStatus ⇒
      provideBuildStatus(buildIdentifier).map(status ⇒ Build(buildIdentifier, status)) pipeTo sender
  }
}

sealed trait JenkinsBuildCommand

case class SetBuildIdentifier(build: BuildIdentifier) extends JenkinsBuildCommand

case object ReadStatus extends JenkinsBuildCommand