package actors.jenkins

import akka.actor.Props
import domain.BuildIdentifier

trait JenkinsBuildActorFactory {
  def newChildActor(buildIdentifier: BuildIdentifier): Props
}