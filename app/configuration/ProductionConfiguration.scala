package configuration

import concurrent.duration._
import domain.jenkins.{JenkinsServerImpl, JenkinsJsonStatusParserImpl}
import domain.BeaconLightStrategyImpl
import akka.actor.ActorRef
import org.eligosource.eventsourced.core.{EventsourcingExtension, Journal}
import org.eligosource.eventsourced.journal.journalio.JournalioJournalProps
import java.io.File

trait ProductionConfiguration extends Configuration {

  val activeTime = (5 seconds)
  val sleepingTime = (5 seconds)

  val jenkinsJsonStatusParser = JenkinsJsonStatusParserImpl
  val jenkinsServer = new JenkinsServerImpl("http://cms-ci:28080")

  val beaconLightStrategy = new BeaconLightStrategyImpl

  lazy val journal: ActorRef = Journal(JournalioJournalProps(new File("target/example-1")))(system)

  lazy val eventsourcedExtension = EventsourcingExtension(system, journal)
}