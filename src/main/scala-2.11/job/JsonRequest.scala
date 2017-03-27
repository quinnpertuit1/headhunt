package job

import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, _}

trait JsonRequest {
  var user = ""
  var jobId = ""

  def parseRequest(req: String): Unit = {
    implicit val formats = DefaultFormats

    val json = parse(req)

    user = (json \ "user").extractOrElse("user")
    jobId = (json \ "jobId").extractOrElse((json \ "id").extract[String])
  }


}
