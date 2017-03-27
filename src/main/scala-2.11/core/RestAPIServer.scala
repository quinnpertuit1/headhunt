package core

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RestAPIServer {

  def run(akkaHTTPPort: Int): Future[Unit] = Future {
    implicit val system = ActorSystem("rest-api")
    implicit val materializer = ActorMaterializer()
    //   implicit val ec = system.dispatcher

    implicit val formats = DefaultFormats

    val route =
      path("job") {
        get {
          complete {
            "List of resumes for the job"
          }
        }
 /*       post {
          entity(as[String]) { newrequest =>
          {
              complete {
                "List of jobs"
              }
          }
          }

        }
        */
      }

    val routes = route
    val bindingFuture = Http().bindAndHandle(routes, "0.0.0.0", akkaHTTPPort)

    println(s"Server online at http://IPADDRESS:%d/\n...".format(akkaHTTPPort))

    Console.readLine() // for the future transformations
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.shutdown()) // and shutdown when done

  }

}
