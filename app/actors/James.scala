/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source
import java.io._

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

  var importingLinks: Boolean = false
  var importedLinks: Int = _
  var linksClient: ActorRef = _
  var linksIterator: Iterator[String] = _
  var linksWorkersFinished: Int = _

  def getPagesLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/raw/enwiki-20151201-page.sql").getLines

  def getLinksLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/raw/enwiki-20151201-pagelinks.sql").getLines

  def getToWork[A <: Command] (iterator: Iterator[String], f: String => A): Unit =
    (1 to workerPoolSize) foreach { _ =>
      if (iterator.hasNext) context.actorOf(N4jImporter.props, genId) ! f(iterator.next)
    }

  def writeToFile (file: String, text: String): Unit = {
    val pw = new PrintWriter(new FileWriter(file, true))
    pw.println(text)
    pw.close
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
      log.info("[James]    Work report! "+importedPages+" imported pages sir, and working hard...")
      if (pagesIterator.hasNext) {
        sender ! ImportPageBatch(pagesIterator.next)
      } else {
        sender ! PoisonPill
        pagesWorkersFinished += 1
        if (pagesWorkersFinished == workerPoolSize) {
          importingPages = false
          log.info("[James]    Finished good sir! Our team of expert actors imported "+importedPages+" pages.")
          pagesClient ! ImportReport(importedPages)
        }
      }

    case ParsePagesToCSV => if (!importingPages) {
      importingPages = true
      pagesClient = sender
      importedPages = 0
      pagesWorkersFinished = 0
      pagesIterator = getPagesLines
      getToWork(pagesIterator, ParsePageCSV.apply)
    }

    case PageCSVReport (csv, imported) => if (imported > 0) {
      importedPages += imported
      writeToFile("./wikidata/csv/pages.csv", csv)
      println("[James]    Work report! "+importedPages+" imported pages sir, and working hard...")
      if (pagesIterator.hasNext) {
        sender ! ParsePageCSV(pagesIterator.next)
      } else {
        sender ! PoisonPill
        pagesWorkersFinished += 1
        if (pagesWorkersFinished == workerPoolSize) {
          importingPages = false
          println("[James]    Finished good sir! Our team of expert actors imported "+importedPages+" pages.")
          pagesClient ! ImportReport(importedPages)
        }
      }
    } else if (pagesIterator.hasNext) {
      sender ! ParsePageCSV(pagesIterator.next)
    }

    case ParseLinksToCSV => if (!importingLinks) {
      Actors.pagesMem ! LoadPages
    }
    //  importingLinks = true
    //  linksClient = sender
    //  importedLinks = 0
    //  linksWorkersFinished = 0
    //  linksIterator = getLinksLines
    //  getToWork(linksIterator, ParseLinkCSV.apply)
    //}

    //case PageCSVReport (csv, imported) =>
    //  importedLinks += imported
    //  writeToFile("./wikidata/csv/links.csv", csv)
    //  println("[James]    Work report! "+importedLinks+" imported links sir, and working hard...")
    //  if (linksIterator.hasNext) {
    //    sender ! ParseLinkCSV(linksIterator.next)
    //  } else {
    //    sender ! PoisonPill
    //    linksWorkersFinished += 1
    //    if (linksWorkersFinished == workerPoolSize) {
    //      importingLinks = false
    //      println("[James]    Finished good sir! Our team of expert actors imported "+importedLinks+" links.")
    //      linksClient ! ImportReport(importedLinks)
    //    }
    //  }
  }
}
