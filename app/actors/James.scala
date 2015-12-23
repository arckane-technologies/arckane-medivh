/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent.Future

import play.api._
import play.api.libs.ws._
import play.api.libs.ws.ning._
import play.api.libs.json._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor._

object James {

  def props: Props = Props(new James)
}

class James extends Actor with ActorLogging {

  def genId: String = java.util.UUID.randomUUID.toString

  def test = for {
    response <- WS.url("https://en.wikipedia.org/wiki/Science")
      .withHeaders("Api-User-Agent" -> "ArckaneMedivh/0.1")
      .get()
  } yield {
    println(response.body)
    response.body
  }

  def receive = {

    case Wake =>
      test
      log.info("James awake.")
  }
}
