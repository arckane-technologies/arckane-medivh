/**
 * @author Francisco Miguel Arámburo Torres - atfm05@gmail.com
 */

package arckane.actors

import scala.concurrent._
import scala.concurrent.duration._
import scala.io.Source

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

  def killAllWorkers (workers: List[ActorRef]): Unit = workers.foreach { worker =>
    worker ! PoisonPill
  }

  def testInput: String = """
    (12499,0,'Gregorio_Allegri','',103,0,0,0.8347045163015879,'20151201035936','20151115062759',689324960,7229,'wikitext'),
    (12500,0,'Goodness_(band)','',1108,0,0,0.747202118623259,'20151102113203','20151115055601',667911449,2661,'wikitext'),
    (12501,0,'Geoff_Hurst','',393,0,0,0.231897074945175,'20151127172802','20151203005858',689324978,36847,'wikitext'),
    (12504,0,'Giovanni_d\'Andrea','',128,0,0,0.714883151198629,'20151106110537','20151115062258',689325028,4494,'wikitext'),
    (12505,0,'Galilean_moons','',273,0,0,0.893303625186257,'20151128101354','20151128101504',691675081,39247,'wikitext'),
    (12506,1,'Graph_theory','',154,0,0,0.0759727117381736,'20151201024647','20151103141256',627275074,3188,'wikitext'),
    (12507,0,'Great_Schism','',3629,0,0,0.321868703069043,'20151031230935','20151031230935',688440420,327,'wikitext'),
    (12509,0,'Gloria_Gaynor','',273,0,0,0.6815572741269671,'20151128143824','20151128122635',691251196,20955,'wikitext'),
    (12511,0,'Gerald_Schroeder','',232,0,0,0.052770157393785294,'20151125122435','20151110204622',677867618,9972,'wikitext'),
    (12514,0,'Ghost','',574,0,0,0.126932085609932,'20151201171223','20151130133756',691568569,81322,'wikitext'),
    (12515,1,'Graffiti','',53,0,0,0.40214280262789,'20151202173018','20151202173111',679659128,25201,'wikitext'),
    (12516,0,'Gibbs_phase_rule','',549,1,0,0.0288070589572257,'20151128141731','20150924004356',584051174,48,'wikitext');"""

  var importing: Boolean = false

  var articlesClient: ActorRef = _

  var importedArticles: Int = 0

  var n4jImporters: List[ActorRef] = _

  var pagesIterator: Iterator[String] = _

  def getPagesLines: Iterator[String] = Source.fromFile(Play.application.path+"/wikidata/raw/enwiki-20151201-page.sql").getLines drop 51

  def fillImportersPool (workers: List[ActorRef], i: Int): List[ActorRef] =
    if (i > 0) {
      fillImportersPool(workers :+ context.actorOf(N4jImporter.props, genId), i - 1)
    } else {
      workers
    }

  def receive = {

    case ImportArticles => if (!importing) {
      importing = true
      articlesClient = sender
      importedArticles = 0
      pagesIterator = getPagesLines
      n4jImporters = fillImportersPool(List.empty[ActorRef], workerPoolSize)
      n4jImporters foreach { worker =>
        if (pagesIterator.hasNext)
          worker ! Process((pagesIterator take 1).next)
      }
    }

    case ImportReport(imported) =>
      importedArticles = importedArticles + imported
      println("Imported: "+importedArticles)
      if (pagesIterator.hasNext) {
        sender ! Process((pagesIterator take 1).next)
      } else {
        killAllWorkers(n4jImporters)
        importing = false
        println("Finished good sir!")
        articlesClient ! ImportReport(importedArticles)
      }
  }
}
