package configuration

import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer}
import concurrent.duration.FiniteDuration
import domain.BeaconLightStrategy

trait Configuration {

  val activeTime: FiniteDuration
  val sleepingTime: FiniteDuration

  val jenkinsJsonStatusParser: JenkinsJsonStatusParser
  val jenkinsServer: JenkinsServer

  val beaconLightStrategy: BeaconLightStrategy
}