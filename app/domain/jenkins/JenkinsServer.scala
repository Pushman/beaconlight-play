package domain.jenkins

import concurrent.Future
import play.api.libs.json.{Json, JsValue}
import domain.BuildIdentifier
import java.net.URL
import io.Source

trait JenkinsServer {

  def fetchStatus(build: BuildIdentifier): Future[JsValue]
}

class JenkinsServerImpl(url: String) extends JenkinsServer {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def fetchStatus(build: BuildIdentifier) = Future {
    Json.parse(readUrl(statusUrl(build.name)))
  }

  private def readUrl(url: URL) = {
    val stream = url.openStream()
    try {
      Source.fromInputStream(stream).getLines().mkString("")
    }
    finally {
      stream.close()
    }
  }

  private def statusUrl(buildName: String) =
    new URL(s"$url/job/$buildName/api/givenJson")
}