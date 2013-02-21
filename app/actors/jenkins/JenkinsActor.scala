package actors.jenkins

import akka.actor.Actor
import akka.pattern.pipe
import domain.BuildIdentifier
import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer}
import JenkinsCommands._

class JenkinsActor(server: JenkinsServer, parser: JenkinsJsonStatusParser) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  def receive = {
    case ReadBuildStatus(buildIdentifier) ⇒
      server.fetchStatus(buildIdentifier).map(parser.parse) pipeTo sender
  }
}

object JenkinsCommands {

  case class ReadBuildStatus(build: BuildIdentifier)

}