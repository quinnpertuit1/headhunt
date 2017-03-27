package job

import scala.concurrent.Future

/**
  * Created by salla on 7/9/16.
  */
trait JobTrait {

  def run()
  def processRequest(req: String): Future[JobResult]
}
