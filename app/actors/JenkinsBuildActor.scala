package actors

import akka.actor.{Props, Actor}
import akka.pattern.pipe
import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer, JenkinsBuildStatusProvider}
import domain.{Build, BuildIdentifier}
import scala.concurrent.ExecutionContext.Implicits.global
import util.LoggedActor

class JenkinsBuildActor(buildIdentifier: BuildIdentifier) extends Actor {
  this: Actor with JenkinsBuildStatusProvider ⇒

  override def receive: Receive = {
    case ReadStatus ⇒
      provideBuildStatus(buildIdentifier).map(status ⇒ Build(buildIdentifier, status)) pipeTo sender
  }
}

trait JenkinsBuildActorFactory {
  def newJenkinsBuildActor(buildIdentifier: BuildIdentifier): Props
}

trait DefaultJenkinsBuildActorFactory extends JenkinsBuildActorFactory {

  def jenkinsServer: JenkinsServer

  def jsonParser: JenkinsJsonStatusParser

  def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) = Props(new JenkinsBuildActor(buildIdentifier) with JenkinsBuildStatusProvider with LoggedActor {
    def provideBuildStatus(build: BuildIdentifier) = jenkinsServer.fetchStatus(build).map(jsonParser.parse)
  })
}

sealed trait JenkinsBuildCommand

case object ReadStatus extends JenkinsBuildCommand