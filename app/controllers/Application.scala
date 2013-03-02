package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import actors.JenkinsStatusReaderCommands.{RegisterObservedBuild, BuildsStatusSummary, ReadBuildsStatuses}
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
import configuration._
import actors.ActorPathKeys
import configuration.{ProductionConfiguration, Configurable}
import domain.BuildIdentifier
import org.eligosource.eventsourced.core.Message
import play.api.mvc.AsyncResult
import play.api.mvc.SimpleResult
import setup.DefaultSetup

object Application extends Controller {

  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  private val setup = new DefaultSetup with Configurable {

    def configuration = new ProductionConfiguration {
      val system = Akka.system
    }
  }

  setup.run()

  private implicit val timeout = Timeout(5 seconds)

  private val statusReader = Akka.system.actorFor(ActorPathKeys.statusReader.path)
  private val buildManager = Akka.system.actorFor(ActorPathKeys.buildsManager.path)

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