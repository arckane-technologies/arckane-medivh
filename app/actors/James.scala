/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source

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

  val workerPoolSize: Int = Play.current.configuration.getInt("james.poolsize").get

  def genId: String = java.util.UUID.randomUUID.toString

  var importingPages: Boolean = false
  var importedPages: Int = _
  var pagesClient: ActorRef = _
  var pagesIterator: Iterator[String] = _
  var pagesWorkersFinished: Int = _

  def getPagesLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/raw/enwiki-20151201-page.sql").getLines drop 51

  def getToWork[A <: Command] (iterator: Iterator[String], f: String => A): Unit =
    (1 to workerPoolSize) foreach { _ =>
      if (iterator.hasNext) context.actorOf(N4jImporter.props, genId) ! f(iterator.next)
    }

  def receive = {

    case ImportPages => if (!importingPages) {
      importingPages = true
      pagesClient = sender
      importedPages = 0
      pagesWorkersFinished = 0
      pagesIterator = getPagesLines take 10
      getToWork(pagesIterator, ImportPageBatch.apply)
    }

    case PageBatchImportReport(imported) =>
      importedPages += imported
      println("[James]    Work report! "+importedPages+" imported pages sir, and working hard...")
      if (pagesIterator.hasNext) {
        sender ! ImportPageBatch(pagesIterator.next)
      } else {
        sender ! PoisonPill
        pagesWorkersFinished += 1
        if (pagesWorkersFinished == workerPoolSize) {
          importingPages = false
          println("[James]    Finished good sir! Our team of expert actors imported "+importedPages+" pages.")
          pagesClient ! ImportReport(importedPages)
        }
      }
  }
}
