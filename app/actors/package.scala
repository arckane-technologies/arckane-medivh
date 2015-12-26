/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane {
  package object actors {

    case object Pin

    case object Pon

    trait Command
    case class ImportPageBatch (payload: String) extends Command
    case class ImportLinkBatch (payload: String) extends Command

    case object ImportPages
    case class ImportReport (imported: Int)

    case class PageBatchImportReport (imported: Int)
  }
}
