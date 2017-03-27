package core

import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, hive}



object Utils {
  var sc: SparkContext = null
  var hiveContext: hive.HiveContext = null
  var sqlContext: SQLContext = null



}
