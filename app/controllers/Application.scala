package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import domain._
import akka.actor.{ActorRef, ActorLogging, Actor, Props}
import actors._
import JenkinsStatusReaderCommands.ReadBuildsStatuses
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import actors.{BeaconLightActor, BuildsManagerCommands, BuildsManagerActor}
import BuildsManagerCommands.CheckStatus
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import concurrent.{Future, Promise}
import java.net.UnknownHostException
import org.eligosource.eventsourced.core._
import java.io.File
import domain.BuildIdentifier
import org.eligosource.eventsourced.core.Message
import play.api.mvc.AsyncResult
import play.api.mvc.SimpleResult
import actors.JenkinsStatusReaderCommands.RegisterObservedBuild
import org.eligosource.eventsourced.journal.journalio.JournalioJournalProps
import actors.JenkinsStatusReaderCommands.BuildsStatusSummary
import configuration.{DefaultActorsConfiguration, ActorPathKeys, ProductionConfiguration}

object Application extends Controller {

  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  val actorsConfiguration = new DefaultActorsConfiguration {
    def configuration = new ProductionConfiguration {}
  }

  implicit val actorRefFactory = Akka.system

  def journal: ActorRef = Journal(JournalioJournalProps(new File("target/example-1")))(Akka.system)

  def eventsourcedExtension = EventsourcingExtension(Akka.system, journal)

  implicit val timeout = Timeout(5 seconds)
  private val activeTime = (5 seconds)
  private val sleepingTime = (5 seconds)

  private val statusReader = eventsourcedExtension.processorOf(actorsConfiguration.actorByPath(ActorPathKeys.statusReader))

  statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server-sonar")))
  statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server")))

  val capsLock = Akka.system.actorOf(Props(new Actor with ActorLogging {

    def receive = {
      case msg => {
        log.info(msg.toString)
        //Toolkit.getDefaultToolkit.setLockingKeyState(VK_CAPS_LOCK, true)
      }
    }
  }))
  private val beaconLightStrategy = new BeaconLightStrategyImpl

  val beaconLight = Akka.system.actorOf(Props(new BeaconLightActor(capsLock, activeTime, sleepingTime)))
  val buildManager = Akka.system.actorOf(Props(new BuildsManagerActor(beaconLight, statusReader, beaconLightStrategy)))

  eventsourcedExtension.recover()

  def index = Action {
    AsyncResult {
      indexView(addBuildForm)
    }
  }

  def checkStatus = Action {
    buildManager ! CheckStatus
    Redirect(routes.Application.index())
  }

  val addBuildForm = Form(
    "buildName" -> text.verifying(nonEmpty)
  )

  def addBuild() = Action {
    implicit request => AsyncResult {
      addBuildForm.bindFromRequest.fold(indexView, onValidBuildName)
    }
  }

  private def onValidBuildName(buildName: String) = {
    statusReader ! Message(RegisterObservedBuild(BuildIdentifier(buildName)))
    Promise.successful(Redirect(routes.Application.index())).future
  }

  private def indexView(form: Form[String]): Future[SimpleResult[_]] =
    readBuildSummary.map(summary =>
      Ok(views.html.index(summary.builds, form))
    ).recover({
      case _: UnknownHostException => InternalServerError("Jenkins host unknown or unavailable")
    })

  private def readBuildSummary =
    (statusReader ? ReadBuildsStatuses).mapTo[BuildsStatusSummary]
}