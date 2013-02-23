package actors.jenkins

import akka.actor.Actor
import akka.pattern.pipe
import domain.jenkins.JenkinsBuildStatusProvider
import domain.{Build, BuildIdentifier}
import scala.concurrent.ExecutionContext.Implicits.global

class JenkinsBuildActor(buildIdentifier: BuildIdentifier) extends Actor {
  this: Actor with JenkinsBuildStatusProvider ⇒

  override def receive: Receive = {
    case ReadStatus ⇒
      provideBuildStatus(buildIdentifier).map(status ⇒ Build(buildIdentifier, status)) pipeTo sender
  }
}

sealed trait JenkinsBuildCommand

case object ReadStatus extends JenkinsBuildCommand