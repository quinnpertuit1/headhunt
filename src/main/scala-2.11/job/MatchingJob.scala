package job

import core.Utils
import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


sealed trait NodeType

case object JOB extends NodeType

case object RESUME extends NodeType

trait VNode{ var nodeType: NodeType}

case class JobNode(var nodeType: NodeType,
                   id: Long) extends VNode

case class ResumeNode(var nodeType: NodeType,
                      id: Long) extends VNode

trait JRScoreTrait { var score: Double}

case class JRScoreSimple(var score: Double) extends JRScoreTrait

case class JobResumeScore(var score: Double,
                          skillScore: Double,
                          titleScore: Double,
                          locationScore: Double,
                          experienceScore: Double,
                          salaryScore: Double
                         ) extends JRScoreTrait

case class ResumeToJobScore(var score: Double,
                            skillScore: Double,
                            titleScore: Double,
                            locationScore: Double,
                            experienceScore: Double,
                            salaryScore: Double
                           ) extends JRScoreTrait

case class JobResumeLink(fScore: JRScoreTrait, rScore: JRScoreTrait)


object MatchingJob extends JobTrait {
  val logger = LoggerFactory.getLogger("com")

  val job_info = "/Users/salla/headhunt/datasets/comp_info.tsv"
  val job_resume = "/Users/salla/headhunt/datasets/ingr_comp.tsv"
  val resume_info = "/Users/salla/headhunt/datasets/ingr_info.tsv"

  var graph: Option[Graph[VNode, JobResumeLink]] = None

  def load(): Option[Graph[VNode, JobResumeLink]] = {
    val st = System.currentTimeMillis()

    logger.info("MatchingJob loading")

    val jobs: RDD[(VertexId, VNode)] =
      Utils.sc.textFile(job_info).
        filter(! _.startsWith("#")).
        map {line =>
          val row = line split '\t'
          (row(0).toLong, JobNode(JOB, row(0).toLong))
        }

    val resumes: RDD[(VertexId, VNode)] =
      Utils.sc.textFile(resume_info).
        filter(! _.startsWith("#")).
        map {line =>
          val row = line split '\t'
          (10000L + row(0).toLong, ResumeNode(RESUME, row(0).toLong))
        }

    val links: RDD[Edge[JobResumeLink]] =
      Utils.sc.textFile(job_resume).
        filter(! _.startsWith("#")).
        map {line =>
          val row = line split '\t'
          Edge(row(0).toLong, 10000L + row(1).toLong, JobResumeLink(JRScoreSimple(0.7), JRScoreSimple(0.6)))
        }


    logger.info("Loaded jobs " + jobs.count)
    logger.info("Loaded resumes " + resumes.count)
    logger.info("Loaded links " + links.count)

    val nodes = jobs ++ resumes

    logger.info("Loaded nodes " + nodes.count)
    val jobNetwork = Graph(nodes, links)


    val end = System.currentTimeMillis()
    logger.info("Loading completed in %d ms".format(end-st))

    Some(jobNetwork)
  }

  override def run(): Unit = {
    graph = load()

    val jobNetwork = graph.get
    println(jobNetwork.vertices.take(10))
    println(jobNetwork.edges.take(10))

    def showTriplet(t: EdgeTriplet[VNode,JobResumeLink]): String = {
      "Job " ++ t.srcId.toString ++ " - fScore: %f , rScore: %f - "
        .format(t.attr.fScore.score, t.attr.rScore.score) ++ " Resume " ++ t.dstId.toString
    }

    jobNetwork.triplets.take(10).
      foreach(showTriplet _ andThen println _)


  }


  def processRequest(req: String): Future[JobResult] = Future {
    val stTime = System.currentTimeMillis()
    val jsonRequest = MatchingRequest.parseRequest(req)

    val jobNetwork = graph.get
    println(jobNetwork.vertices.take(10))
    println(jobNetwork.edges.take(10))

    def showTriplet(t: EdgeTriplet[VNode,JobResumeLink]): String = {
      "Job " ++ t.srcId.toString ++ " - fScore: %f , rScore: %f - "
        .format(t.attr.fScore.score, t.attr.rScore.score) ++ " Resume " ++ t.dstId.toString
    }

    jobNetwork.triplets.take(10).
      foreach(showTriplet _ andThen println _)


    val endTime = System.currentTimeMillis()
    logger.info("[%s] : Started in Time taken: %d ms".format(
      jsonRequest.jobId,
      (endTime - stTime)
    ))

    val jobResult = JobResult(jsonRequest.jobId, 0, (endTime - stTime))

    jobResult
  }
}
