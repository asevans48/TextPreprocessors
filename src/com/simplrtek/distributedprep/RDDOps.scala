package com.simplrtek.distributedprep

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf,SparkContext}
import com.simplrtek.preprocessors.SentTokenizer

/**
 * A set of operations for converting to Mahout, saving,
 * and other techniques.
 */
object RDDOps {
  
  def save[T](rdd : RDD[T],path : String)={
    rdd.saveAsTextFile(path)
  }//save
  
}