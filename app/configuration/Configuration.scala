package configuration

import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer}
import concurrent.duration.FiniteDuration
import domain.BeaconLightStrategy
import akka.actor.{ActorSystem, ActorRef}
import org.eligosource.eventsourced.core.EventsourcingExtension

trait Configuration {

  val activeTime: FiniteDuration
  val sleepingTime: FiniteDuration

  val jenkinsJsonStatusParser: JenkinsJsonStatusParser
  val jenkinsServer: JenkinsServer

  val beaconLightStrategy: BeaconLightStrategy

  val journal: ActorRef

  val system: ActorSystem

  val eventsourcedExtension: EventsourcingExtension
}