/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._

import play.api.libs.json._

import akka.actor._

import arckane.db.transaction._

object N4jImporter {

  def props: Props = Props(new N4jImporter)
}

class N4jImporter extends Actor with ActorLogging {

  import context.dispatcher
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.duration._
  implicit val timeout = Timeout(1 seconds)

  /* https://upload.wikimedia.org/wikipedia/commons/f/f7/MediaWiki_1.24.1_database_schema.svg */
  val ArticleRegex = """\((\d+),0,'((?:\\'|[^'])+)','',\d+,0,[^\)]+\)""".r

  val EdgeRegex = """\((\d+),0,'((?:\\'|[^'])+)',0\)""".r

  val DisambiguationRegexp = """.+\(disambiguation\).*""".r

  def importPageBatch (pages: JsArray): Future[TxResult] = query(Json.obj(
    "statement" ->
      "FOREACH (page in {pages} | MERGE (:Wikipedia {pageId:page.p, namespace:page.n, title:page.t}))",
    "parameters" -> Json.obj(
      "pages" -> pages
    )))

  def pagesParamsFromString (input: String): (JsArray, Int) = ArticleRegex.findAllIn(input).foldLeft ((Json.arr(), 0)) {
    case ((params, count), ArticleRegex(pageId, title)) =>
      (params :+ Json.obj(
        "p" -> pageId.toInt,
        "t" -> title.replace("\\", "").replace("_", " ")
      ), count + 1)
  }

  /*
   * Only articles, non-redirects, doesn't have "(disambiguation)" on its name.
   * (there are still disambiguation pages without (desambiguation) on its name.)
   */
  def pagesToCSV (input: String): (String, Int) = ArticleRegex.findAllIn(input).foldLeft (("", 0)) {
    case ((csv, count), ArticleRegex(pageId, title)) => title match {
      case DisambiguationRegexp(_*) => (csv, count)
      case _ =>
        (csv
          + pageId + ","
          + "\"" + title.replace("\\", "").replace("\"", "\\\"") + "\"\n"
        , count + 1)
    }
  }

  def linksToCSV (input: String): (String, Int) = EdgeRegex.findAllIn(input).foldLeft (("", 0)) {
    case ((csv, count), EdgeRegex(fromId, toTitle)) => toTitle match {
      case DisambiguationRegexp(_*) =>
        (csv, count)
      case _ =>
        val title = toTitle.replace("\\", "").replace("\"", "\\\"")
        (for {
          _ <- Await.result(Actors.pagesMem ? GetTitle(fromId), 1 seconds).asInstanceOf[Option[String]]
          a <- Await.result(Actors.pagesMem ? GetId(title), 1 seconds).asInstanceOf[Option[String]]
        } yield a) match {
          case None => (csv, count)
          case Some(toId) => (csv + fromId + "," + toId + "\n" , count + 1)
        }
    }
  }

  def receive = {

    case ImportPageBatch (string) =>
      val (pages, count) = pagesParamsFromString(string)
      val sen = sender
      importPageBatch(pages).map { result =>
        sen ! ImportReport(count)
      }

    case ParsePageCSV (string) =>
      val (csv, count) = pagesToCSV(string)
      sender ! PageCSVReport(csv, count)

    case ParseLinkCSV (string) =>
      val (csv, count) = linksToCSV(string)
      sender ! LinkCSVReport(csv, count)
  }
}
