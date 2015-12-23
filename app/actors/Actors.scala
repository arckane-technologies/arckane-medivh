/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import play.api._
import play.api.libs.concurrent.Akka
import akka.actor.ActorSystem

import play.api._
import play.api.libs.concurrent.Akka
import akka.actor._
import javax.inject.Inject

class Actors @Inject() (implicit app: Application) extends Plugin {

  lazy val james = Akka.system.actorOf(James.props, "james")
}

object Actors {
  def james: ActorRef = Play.current.plugin[Actors]
    .getOrElse(throw new RuntimeException("Actors plugin not loaded"))
    .james
}
