package job

import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods._
import org.slf4j.LoggerFactory

class MatchingRequest extends JsonRequest {
  val logger = LoggerFactory.getLogger("com")

  override def parseRequest(req: String): Unit = {
    super.parseRequest(req)
    implicit val formats = DefaultFormats

    val json = parse(req)

  }

}

object MatchingRequest {
  def parseRequest(req: String): MatchingRequest = {
    val jreq = new MatchingRequest
    jreq.parseRequest(req)
    jreq
  }
}

