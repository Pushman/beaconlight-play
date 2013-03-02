package configuration.actors

object ActorPathKeys {

  trait ActorPathKey {
    def name: String

    def path: String
  }

  case class DefaultActorPathKey(path: String, name: String) extends ActorPathKey

  object ActorPathKey {
    implicit def toDefaultActorPathKey(parentAndName: (ActorPathKey, String)) = parentAndName match {
      case (parent, name) => DefaultActorPathKey(s"${parent.path}/$name", name)
    }
  }

  import ActorPathKey._

  private val base: ActorPathKey = DefaultActorPathKey("/user", "user")
  val beaconLight: ActorPathKey = (base -> "beaconLight")
  val buildsManager: ActorPathKey = (base -> s"buildsManager")
  val capsLock: ActorPathKey = (base -> s"capsLock")
  val statusReader: ActorPathKey = (base -> s"statusReader")
}
