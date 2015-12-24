/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

import scala.concurrent.duration._

import play.api.Play
import play.api.test.FakeApplication
import play.api.libs.json._

import akka.actor._
import akka.testkit._
import org.scalatest._

import arckane.actors._

class JamesActorSpec (_system: ActorSystem) extends TestKit(_system)
with ImplicitSender
with WordSpecLike
with Matchers
with BeforeAndAfterAll {

  def this() = this({
    val app = FakeApplication(additionalConfiguration = Map(
      "neo.address" -> "http://localhost:7474",
      "neo.user" -> "neo4j",
      "neo.password" -> "admin"))
    Play.start(app)
    app.actorSystem
  })

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "James" should {

    "echo" in {
      Actors.james ! Pin
      expectMsg(Pon)
    }
  }
}
