/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.collection.immutable.TreeMap
import scala.io.Source

import play.api._
import play.api.Play.current

import akka.actor._

object PagesMem {

  def props: Props = Props(new PagesMem)
}

class PagesMem extends Actor with ActorLogging {

  var memory: TreeMap[String, String] = _

  val CSVRegex = """(\d+),"((?:\\"|[^"])+)".*""".r

  def pagesCSVLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/csv/pages.csv").getLines

  def loadPages: (TreeMap[String, String], Int) = pagesCSVLines.foldLeft((TreeMap.empty[String, String], 0)) {
    case ((mem, count), line) =>
      line match {
        case CSVRegex(pageid, title) =>
          if (count % 250000 == 0) println("[PagesMem]   Loaded " + count + " pages...")
          (mem + (title -> pageid, pageid -> title), count + 1)
        case _ =>
          (mem, count)
      }
  }

  def receive = {

    case LoadPages =>
      println("[PagesMem]   Loading pages csv...")
      val (mem, count) = loadPages
      memory = mem
      println("[PagesMem]   Loading complete! Loaded " + count + " pages.")
      sender ! LoadPagesFinished

    case GetTitle (pageid) =>
      sender ! memory.get(pageid)

    case GetId (title) =>
      sender ! memory.get(title)
  }
}
