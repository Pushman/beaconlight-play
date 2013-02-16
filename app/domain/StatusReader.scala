package domain


trait StatusReader {

  def readBuild(build: BuildIdentifier): BuildStatus
}

case class BuildStatus(isBroken: Boolean, isInProgress: Boolean)
