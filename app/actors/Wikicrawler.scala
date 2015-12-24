/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.matching.Regex
import scala.util.matching.Regex.MatchIterator

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
  type Url = String
  type Title = String
  type Wiki = (Url, Title)
  type Articles = List[Wiki]
  type Categories = List[Wiki]

  val WikiAnchor = """<a\shref="\/wiki\/([^"]*)"\stitle="([^"]+)"[^>]*>""".r
  val IsCategory = """^Category:.+""".r

  def getPageLinks (url: String) = for {
    response <- WS.url(url)
      .withHeaders("Api-User-Agent" -> "ArckaneMedivh/0.1")
      .get()
  } yield extract(WikiAnchor.findAllIn(response.body))

  def extract (matches: MatchIterator): (Articles, Categories) =
    matches.foldLeft((List.empty[Wiki], List.empty[Wiki])) { case ((articles, categories), line) =>
      line match {
        case WikiAnchor(path, title) => path match {
          case IsCategory() =>
            (articles, categories :+ ("https://en.wikipedia.org/wiki/"+path, title))
          case _ =>
            (articles :+ ("https://en.wikipedia.org/wiki/"+path, title), categories)
        }
        case _ => (articles, categories)
      }
    }

  def receive = {

    case Process(url) =>
      val (articles, categories) = Await.result(getPageLinks(url), 5 seconds)
      println(articles.length)
  }
}
