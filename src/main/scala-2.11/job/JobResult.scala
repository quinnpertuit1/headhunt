package job

case class JobResult(jobId: String, nResults: Long, tTaken: Long, outPath: String = "") {
  var content: Option[String] = None

  def getJson: String = content.getOrElse(this.fieldToJson)

  def fieldToJson: String = {
    "{\"jobId\":\"%s\", \"nResults\":\"%d\", \"tTaken\":\"%d\", \"outPath\":\"%s\" }"
      .format(this.jobId, this.nResults, this.tTaken, this.outPath)
  }
}
