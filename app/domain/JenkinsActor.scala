package domain

import akka.actor.Actor

class JenkinsActor extends Actor {
  def receive = ???
}

object JenkinsCommands {

  case class ReadBuildStatus(build: BuildIdentifier)

}