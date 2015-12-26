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

  val EdgeRegex = """\((\d+),(0|14),'((?:\\'|[^'])+)','',\d+,0,[^\)]+\)""".r

  def importPageBatch (pages: JsArray): Future[TxResult] = query(Json.obj(
    "statement" ->
      "FOREACH (page in {pages} | MERGE (:Wikipedia {pageId:page.p, namespace:page.n, title:page.t}))",
    "parameters" -> Json.obj(
      "pages" -> pages
    )))

  def pagesParamsFromString (input: String): (JsArray, Int) = EdgeRegex.findAllIn(input).foldLeft ((Json.arr(), 0)) {
    case ((params, count), EdgeRegex(pageId, namespace, title)) =>
        (params :+ Json.obj(
          "p" -> pageId.toInt,
          "n" -> namespace.toInt,
          "t" -> title.replace("\\", "").replace("_", " ")
        ), count + 1)
  }

  def receive = {

    case ImportPageBatch (string) =>
      val (pages, count) = pagesParamsFromString(string)
      val sen = sender
      importPageBatch(pages).map { result =>
        sen ! ImportReport(count)
      }
  }
}
