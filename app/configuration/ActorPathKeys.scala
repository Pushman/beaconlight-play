package configuration


object ActorPathKeys {

  type ActorPathKey = String

  private val base: ActorPathKey = "/user"
  val statusReader: ActorPathKey = s"$base/statusReader"
  val jenkinsBuild: ActorPathKey = s"$base/statusReader"
}
