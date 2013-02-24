package domain

import actors.{JenkinsStatusReaderCommands, BeaconLightCommands}
import BeaconLightCommands.{BeaconLightAction, Stop, Activate}
import JenkinsStatusReaderCommands.BuildsStatusSummary

trait BeaconLightStrategy {
  def commandFor(builds: BuildsStatusSummary): BeaconLightAction
}

class BeaconLightStrategyImpl extends BeaconLightStrategy {

  override def commandFor(summary: BuildsStatusSummary) =
    if (containsUnhandledBrokenBuild(summary.builds))
      Activate
    else
      Stop

  private def containsUnhandledBrokenBuild(builds: Set[Build]) =
    containsBrokenBuild(builds) && !containsBuildInProgress(builds)

  private def containsBrokenBuild(builds: Set[Build]) =
    builds.exists(_.status.isFailed)

  private def containsBuildInProgress(builds: Set[Build]) =
    builds.exists(_.status.isInProgress)
}