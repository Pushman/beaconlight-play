package configuration

import concurrent.duration._
import domain.jenkins.{JenkinsServerImpl, JenkinsJsonStatusParserImpl}
import domain.BeaconLightStrategyImpl

trait ProductionConfiguration extends Configuration {

  val activeTime = (5 seconds)
  val sleepingTime = (5 seconds)

  val jenkinsJsonStatusParser = JenkinsJsonStatusParserImpl
  val jenkinsServer = new JenkinsServerImpl("http://cms-ci:28080")

  val beaconLightStrategy = new BeaconLightStrategyImpl
}