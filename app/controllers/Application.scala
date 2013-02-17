package controllers

import play.api.mvc._
import play.api.libs.concurrent.Akka
import domain.{Build, BuildIdentifier, StatusReaderActor, BuildsManagerActor}
import domain.jenkins.{JenkinsJsonStatusParserImpl, JenkinsServerImpl, JenkinsActor}
import akka.actor.{ActorLogging, Actor, Props}
import domain.StatusReaderCommands.{RegisterObservedBuild, BuildsStatusSummary, ReadBuildsStatuses}
import akka.pattern.ask
import akka.util.Timeout
import concurrent.duration._
import util.LoggedActor

object Application extends Controller {

  import play.api.Play.current
  import scala.concurrent.ExecutionContext.Implicits.global

  val jenkinsServer = new JenkinsServerImpl("http://cms-ci:28080")
  val jenkinsActor = Akka.system.actorOf(Props(new JenkinsActor(jenkinsServer, JenkinsJsonStatusParserImpl) with LoggedActor))
  val statusReader = Akka.system.actorOf(Props(new StatusReaderActor(jenkinsActor) with LoggedActor))
  
  statusReader ! RegisterObservedBuild(BuildIdentifier("transfolio-cms-server-sonar"))
  statusReader ! RegisterObservedBuild(BuildIdentifier("transfolio-cms-server"))
  
  implicit val timeout = Timeout(5 seconds)

  val beaconLight = Akka.system.actorOf(Props(new Actor with ActorLogging {
    def receive = {
      case msg => log.info(msg.toString)
    }
  }))

  val buildManager = Akka.system.actorOf(Props(new BuildsManagerActor(beaconLight, statusReader)))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def builds = Action {
    val builds = (statusReader ? ReadBuildsStatuses)
    AsyncResult {
      builds.map(b => 
        Ok(views.html.builds(b.asInstanceOf[BuildsStatusSummary].builds))
      )
    }
  }
}