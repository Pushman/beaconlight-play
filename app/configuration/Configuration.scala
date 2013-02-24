package configuration

import domain.jenkins.{JenkinsJsonStatusParser, JenkinsServer}

trait Configuration {

  def jenkinsJsonStatusParser: JenkinsJsonStatusParser

  def jenkinsServer: JenkinsServer
}