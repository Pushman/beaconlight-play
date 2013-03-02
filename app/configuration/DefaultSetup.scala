package configuration

import org.eligosource.eventsourced.core.Message
import actors.JenkinsStatusReaderCommands.RegisterObservedBuild
import domain.BuildIdentifier
import actors.BuildsManagerCommands.CheckStatus

trait DefaultSetup extends Setup {
  this: Setup with Configurable =>

  private val actorsFactory = new DefaultActorsFactory with DefaultActorsConfiguration {
    def configuration = DefaultSetup.this.configuration
  }

  def run() {
    actorsFactory.createActor(ActorPathKeys.beaconLight)
    actorsFactory.createActor(ActorPathKeys.capsLock)
    val statusReader = actorsFactory.createActor(ActorPathKeys.statusReader)
    val buildManager = actorsFactory.createActor(ActorPathKeys.buildsManager)

    statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server-sonar")))
    statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server")))

    configuration.eventsourcedExtension.recover()

    buildManager ! CheckStatus
  }
}