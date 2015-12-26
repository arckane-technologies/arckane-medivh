/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane {
  package object actors {

    case object Pin

    case object Pon

    case object LoadPages
    case object LoadPagesFinished
    case class GetTitle (pageid: String)
    case class GetPageid (title: String)

    trait Command
    case class ImportPageBatch (payload: String) extends Command
    case class ImportLinkBatch (payload: String) extends Command
    case class ParsePageCSV (payload: String) extends Command
    case class ParseLinkCSV (payload: String) extends Command

    case object ImportPages
    case class ImportReport (imported: Int)
    case object ParsePagesToCSV
    case object ParseLinksToCSV

    case class PageBatchImportReport (imported: Int)
    case class PageCSVReport (payload: String, imported: Int)
  }
}
