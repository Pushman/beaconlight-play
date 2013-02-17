package domain

import akka.actor.Actor

class StatusReaderActor extends Actor{
  def receive = ???
}

case class BuildStatus(isBroken: Boolean, isInProgress: Boolean)

object StatusReaderCommands {

  case class ReadBuildStatuses(builds: Set[BuildIdentifier])
}