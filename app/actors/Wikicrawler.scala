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

object Wikicrawler {

  def props: Props = Props(new Wikicrawler)
}

class Wikicrawler extends Actor with ActorLogging {

  def receive = {

    case Wake => log.info("Wikicrawler awake.")
  }
}
