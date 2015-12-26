/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.collection.immutable.HashMap
import scala.io.Source

import play.api._
import play.api.Play.current

import akka.actor._

object PagesMem {

  def props: Props = Props(new PagesMem)
}

class PagesMem extends Actor with ActorLogging {

  var idMapTitle: HashMap[String, String] = _
  var titleMapId: HashMap[String, String] = _

  val CSVRegex = """(\d+),\"((?:\\"|[^"])+)\",.*""".r

  def pagesCSVLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/csv/pages.csv").getLines

  def receive = {

    case LoadPages =>
      println("[PagesMem]   Loading pages csv...")
      val (hashA, hashB) = pagesCSVLines.foldLeft((HashMap.empty[String, String], HashMap.empty[String, String])) {
        case ((hashA, hashB), line) =>
          line match {
            case CSVRegex(pageid, title) =>
              (hashA + (pageid -> title), hashB + (title -> pageid))
            case _ =>
              println(line)
              println("that was one")
              (hashA, hashB)
          }
      }
      idMapTitle = hashA
      titleMapId = hashB
      println("[PagesMem]   Loading complete!")
      println(hashA.size)
      sender ! LoadPagesFinished

    case GetTitle (pageid) =>
      sender ! idMapTitle.get(pageid)

    case GetPageid (title) =>
      sender ! titleMapId.get(title)
  }
}
