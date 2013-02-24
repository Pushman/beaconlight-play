package configuration

import domain.jenkins.{JenkinsServerImpl, JenkinsJsonStatusParserImpl}

trait ProductionConfiguration extends Configuration {
  def jenkinsJsonStatusParser = JenkinsJsonStatusParserImpl

  def jenkinsServer = new JenkinsServerImpl("http://cms-ci:28080")
}