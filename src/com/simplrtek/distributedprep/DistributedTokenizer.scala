package com.simplrtek.distributedprep

import scala.collection.JavaConversions._
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf,SparkContext}
import com.simplrtek.preprocessors.SentTokenizer

class DistributedTokenizer(hadoopDir:String = "C:\\Users/packe/Documents/hadoop-2.7.1",mRS:String ="15g",emx:String = "8g",dmx:String="4g"){
  System.setProperty("hadoop.home.dir", "C:\\Users/packe/Documents/hadoop-2.7.1")
  var conf:SparkConf = new SparkConf().setMaster("local").setAppName("TestApp")
  conf=conf.set("spark.driver.maxResultSize", mRS)
  conf=conf.set("spark.executor.memory",emx)
  conf=conf.set("spark.driver.memory",dmx)
  val sc:SparkContext = new SparkContext(conf)
  
  
  /**
   * A list of strings to tokenize.
   * 
   * @param		text		The list of texts.
   * @return	An RDD containing arrays of strings to be tokenized.
   */
  def tokenize(texts:List[String]):RDD[Array[String]]={
    val parts = sc.parallelize(texts)
    parts.mapPartitions(part => part.map({ x => SentTokenizer.getSentences(x)}))
  }
  
}