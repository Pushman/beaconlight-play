package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import actors._
import JenkinsStatusReaderCommands.ReadBuildsStatuses
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import actors.BuildsManagerCommands
import BuildsManagerCommands.CheckStatus
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import concurrent.{Future, Promise}
import java.net.UnknownHostException
import domain.BuildIdentifier
import org.eligosource.eventsourced.core.Message
import play.api.mvc.AsyncResult
import play.api.mvc.SimpleResult
import actors.JenkinsStatusReaderCommands.RegisterObservedBuild
import actors.JenkinsStatusReaderCommands.BuildsStatusSummary
import configuration.{DefaultActorsConfiguration, ActorPathKeys, ProductionConfiguration}

object Application extends Controller {

  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  private val configuration = new ProductionConfiguration {
    val system = Akka.system
  }

  private val actorsConfiguration = new DefaultActorsConfiguration {
    def configuration = Application.configuration
  }

  private implicit val actorRefFactory = Akka.system

  private implicit val timeout = Timeout(5 seconds)

  actorsConfiguration.createActor(ActorPathKeys.capsLock)
  actorsConfiguration.createActor(ActorPathKeys.beaconLight)
  private val statusReader = actorsConfiguration.createActor(ActorPathKeys.statusReader)

  statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server-sonar")))
  statusReader ! Message(RegisterObservedBuild(BuildIdentifier("transfolio-cms-server")))

  private val buildManager = actorsConfiguration.createActor(ActorPathKeys.buildsManager)

  configuration.eventsourcedExtension.recover()

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