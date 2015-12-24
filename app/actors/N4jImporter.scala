/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._
import scala.concurrent.duration._

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

import akka.actor._

import scala.util.matching.Regex
import scala.util.matching.Regex.MatchIterator

import arckane.db.transaction._

object N4jImporter {

  def props: Props = Props(new N4jImporter)
}

class N4jImporter extends Actor with ActorLogging {

  val EdgeRegex = """\(([0-9]+),[0-9]+,'((?:\\\'|[^'])+)',[^\)]+\)""".r

  def process (input: String)(f: (Int, String) => Unit): Unit = EdgeRegex.findAllIn(input) foreach {
    case EdgeRegex(pageId, pageTitle) => f(pageId.toInt, pageTitle.replace("\\", "").replace("_", " "))
  }

  def receive = {

    case Process(string) =>
      var count = 0
      var last = ""
      process(string) { (articleId, articleTitle) =>
        count = count + 1
        last = articleTitle
      }
      println(last)
      sender ! ImportReport(count)
  }
}
