package testing

import core.Utils
import job.{JobResult, JobTrait}
import org.apache.spark.graphx._
import org.apache.spark.graphx.lib.ShortestPaths
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object ShortestPathJob extends JobTrait {
  val logger = LoggerFactory.getLogger("com")

  trait Node{ var id: String}

  case class Resume(var id: String, skills: String) extends Node

  case class Job(var id: String, skills: String) extends Node

  override def run(): Unit = {
    logger.info("TestJob run")

    val nodes: RDD[(VertexId, Node)] =
      Utils.sc.parallelize(Array(
        (1L, Resume("sridhar", "java")),
        (2L, Resume("pete", "java")),
        (3L, Resume("shekhar", "java")),
        (4L, Resume("john", "html")),

        (101L, Job("job1", "java")),
        (102L, Job("job2", "html"))
      ))

    val links: RDD[Edge[String]] =
      Utils.sc.parallelize(Array(
        Edge(101L, 1L, "hire"),
        Edge(101L, 2L, "hire"),
        Edge(101L, 3L, "hire"),
        Edge(102L, 4L, "hire")
      ))

    val graph = Graph(nodes, links)

    val res = graph.edges.filter { case Edge(src, dst, prop) => prop.equals("hiring") }.take(10)

    res.foreach(r => logger.info(r.toString))

    val facts: RDD[String] = graph.triplets.map(triplet => triplet.srcAttr.id +
      " is the " + triplet.attr + " of " + triplet.dstAttr.id)
    facts.collect.foreach(println(_))

    val ns = graph.collectNeighbors(EdgeDirection.Either)
    ns.take(10)

    graph.connectedComponents()

    graph.vertices.take(10)
    graph.edges.take(10)
    graph.connectedComponents.vertices.take(10)
    graph.connectedComponents.edges.take(10)



    val v1 = 101L
    val v2 = 1L

    val result = ShortestPaths.run(graph, Seq(v2))
    val shortestPath = result               // result is a graph
      .vertices                             // we get the vertices RDD
      .filter({case(vId, _) => vId == v1})  // we filter to get only the shortest path from v1
      .first                                // there's only one value
      ._2                                   // the result is a tuple (v1, Map)
      .get(v2)                              // we get its shortest path to v2 as an Option object


  }

  def processRequest(req: String): Future[JobResult] = Future {
    val jobResult = JobResult("jobId", 0, 0)

    jobResult
  }

}
