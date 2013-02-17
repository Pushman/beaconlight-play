package domain

case class Build(identifier: BuildIdentifier, status: BuildStatus)

case class BuildIdentifier(name: String)

case class BuildStatus(isFailed: Boolean, isInProgress: Boolean)