package com.simplrtek.distributedprep

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.linalg.Vector
import org.apache.spark.SparkConf

class DistributedTFIDFVectorizer(hadoopDir : String,mRS:String ="15g",emx:String = "8g",dmx:String="4g"){
  
  System.setProperty("hadoop.home.dir", hadoopDir)
  var conf:SparkConf = new SparkConf().setMaster("local").setAppName("TestApp")
  conf=conf.set("spark.driver.maxResultSize", mRS)
  conf=conf.set("spark.executor.memory",emx)
  conf=conf.set("spark.driver.memory",dmx)
  val sc:SparkContext = new SparkContext(conf)
  
  def getAsMahoutVector()={
    
  }
  
  def vectorize(parquetPath : String)={
    val rdd = sc.textFile(parquetPath)
    
    
  }
}