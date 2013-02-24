package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import domain._
import domain.jenkins.{JenkinsBuildStatusProvider, JenkinsJsonStatusParserImpl, JenkinsServerImpl}
import akka.actor.{ActorLogging, Actor, Props}
import actors._
import play.api.mvc.SimpleResult
import JenkinsStatusReaderCommands.ReadBuildsStatuses
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import util.LoggedActor
import actors.{JenkinsStatusReaderActor, BeaconLightActor, BuildsManagerCommands, BuildsManagerActor}
import BuildsManagerCommands.CheckStatus
import JenkinsStatusReaderCommands.RegisterObservedBuild
import domain.BuildIdentifier
import play.api.mvc.AsyncResult
import JenkinsStatusReaderCommands.BuildsStatusSummary
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import concurrent.{Future, Promise}
import java.net.UnknownHostException

object Application extends Controller {

  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5 seconds)
  private val activeTime = (5 seconds)
  private val sleepingTime = (5 seconds)

  private val jenkinsServer = new JenkinsServerImpl("http://cms-ci:28080")
  private val statusReader = Akka.system.actorOf(Props(new JenkinsStatusReaderActor with JenkinsBuildActorFactory with LoggedActor {
    def newJenkinsBuildActor(buildIdentifier: BuildIdentifier) = Props(new JenkinsBuildActor(buildIdentifier) with JenkinsBuildStatusProvider with LoggedActor {
      def provideBuildStatus(build: BuildIdentifier) = jenkinsServer.fetchStatus(build).map(JenkinsJsonStatusParserImpl.parse)
    })
  }))

  statusReader ! RegisterObservedBuild(BuildIdentifier("transfolio-cms-server-sonar"))
  statusReader ! RegisterObservedBuild(BuildIdentifier("transfolio-cms-server"))

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
    statusReader ! RegisterObservedBuild(BuildIdentifier(buildName))
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