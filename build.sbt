name := "headhunt"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val sparkVersion = "1.6.2"
  val jsonVersion = "3.2.10"
  val mongoVersion = "3.1.0"
  val elasticVersion = "2.3.3"
  val akkaVersion = "2.3.15"

  Seq(

    "org.json"               % "json"               % "20090211",
    "org.jsoup"              % "jsoup"              % "1.8.3",
    "org.elasticsearch"       % "elasticsearch"     % "1.4.2",
    "log4j" % "log4j" % "1.2.17",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.7.1",
    "org.apache.hadoop" % "hadoop-core" % "1.2.1",
    "org.apache.hadoop" % "hadoop-client" % "2.4.0",
    "org.apache.hadoop" % "hadoop-hdfs" % "2.4.0",

  "com.couchbase.client" % "couchbase-client" % "1.4.4",

    "org.apache.spark" % "spark-streaming_2.10" % sparkVersion,
    "org.apache.spark" % "spark-core_2.10" % sparkVersion,
    "org.apache.spark" % "spark-sql_2.10" % sparkVersion,
    "org.apache.spark" % "spark-mllib_2.10" % sparkVersion,
    "org.apache.spark" % "spark-streaming-kafka_2.10" % sparkVersion,
    "org.apache.spark" % "spark-hive_2.10" % sparkVersion ,
    "org.apache.spark" % "spark-yarn_2.10" % sparkVersion,

    "org.slf4j" % "slf4j-api" % "1.7.21",
    "org.json4s" % "json4s-jackson_2.10" % jsonVersion,
    "org.json4s" % "json4s-native_2.10" % jsonVersion,
    "org.json4s" % "json4s-ast_2.10" % jsonVersion,

    "org.mongodb" % "mongo-java-driver" % mongoVersion,
    "org.elasticsearch" % "elasticsearch-spark_2.10" % elasticVersion,

    "io.spray" % "spray-json_2.10" % "1.3.2",

    "com.typesafe.akka" % "akka-slf4j_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-actor_2.10" % akkaVersion,
    "com.typesafe.akka" % "akka-http-experimental_2.10" % "2.0.4"

  )
}
