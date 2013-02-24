package actors

import akka.actor.Actor
import concurrent.duration._
import BuildsManagerCommands._
import akka.pattern.ask
import akka.util.Timeout
import concurrent.Future
import JenkinsStatusReaderCommands.{BuildsStatusSummary, ReadBuildsStatuses}
import domain.BeaconLightStrategy
import akka.pattern.pipe
import configuration.ActorPathKeys

class BuildsManagerActor(beaconLightStrategy: BeaconLightStrategy) extends Actor {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)

  private val beaconLight = context.actorFor(ActorPathKeys.beaconLight.path)
  private val statusReader = context.actorFor(ActorPathKeys.statusReader.path)

  private var enabled: Boolean = true

  override def receive = {
    case CheckStatus if enabled ⇒
      readBuildStatuses.map(beaconLightStrategy.commandFor) pipeTo beaconLight

    case Enable ⇒ enabled = true
    case Disable ⇒ enabled = false
  }

  def readBuildStatuses: Future[BuildsStatusSummary] =
    (statusReader ? ReadBuildsStatuses).mapTo[BuildsStatusSummary]
}

object BuildsManagerCommands {

  case object CheckStatus

  case object Disable

  case object Enable

}