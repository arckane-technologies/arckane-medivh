/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import akka.actor._

object James {

  def props: Props = Props(new James)
}

class James extends Actor with ActorLogging {

  def genId: String = java.util.UUID.randomUUID.toString

  def receive = {

    case Wake => log.info("James awake.")
  }
}
