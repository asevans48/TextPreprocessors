package com.simplrtek.preprocessors

import org.apache.spark.rdd.RDD

import java.util.Properties

import java.io.File
import sbt.io.IO
import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf,SparkContext}

/**
 * Distributes the Text Tiling algorithm via Spark.
 */
class DistributedTextTiler (hadoopDir:String = "C:\\Users/packe/Documents/hadoop-2.7.1",mRS:String ="15g",emx:String = "8g",dmx:String="4g"){
  System.setProperty("hadoop.home.dir", "C:\\Users/packe/Documents/hadoop-2.7.1")
  var conf:SparkConf = new SparkConf().setMaster("local").setAppName("TestApp")
  conf=conf.set("spark.driver.maxResultSize", mRS)
  conf=conf.set("spark.executor.memory",emx)
  conf=conf.set("spark.driver.memory",dmx)
  val sc:SparkContext = new SparkContext(conf)
  
  
  /**
   * Stem a list of sentences using spark.
   * 
   * @param			texts		A list of strings to lemmatize
   * @return		An RDD[Seq[String]] containing the lemmatized strings.	 
   */
  def stem(texts:List[String]):RDD[Array[String]]={
    val parts = sc.parallelize(texts)
    var tiler:TextTiler = new TextTiler()
    val res = parts.mapPartitions(part=>{
      part.map {tiler.segment(_).toArray}
    })
    res
  }
  
  /**
   * Write texts to file as opposed to return an RDD.
   * 
   * @param		inDirectory										The input directory to get files from.
   * @param		outDirectory									The output directory to write files to.
   * @param		{Integer}{maxTextsInMemory}		The maximum number of texts to sore in memory at once.
   */
  def writeToFile(inDirectory:String,outDirectory:String,maxTextsInMemory:Integer = 1000)={
    val files = IO.listFiles(new File(inDirectory)).toList
    var start:Integer = 0
    var end:Integer = if(maxTextsInMemory < files.size) maxTextsInMemory else files.size - 1
    var f:Integer =0
    while(start < files.size){
      stem(files.slice(start, end).map { x => IO.read(x) }).foreach {
          arr => arr.foreach { 
            var ifn:Integer =0
            str => IO.write(new File(outDirectory,"%i_%i.txt".format(f,ifn)),str.getBytes)
            ifn = ifn + 1
          } 
        }
        f = f+1
        start += maxTextsInMemory
        end += maxTextsInMemory
    }
    
  }
  
}