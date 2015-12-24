/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane {
  package object actors {

    case object Pin

    case object Pon

    case class Process (payload: String)

    case object ImportArticles

    case class ImportReport (imported: Int)
  }
}
