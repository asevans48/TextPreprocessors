package com.simplrtek.preprocessors

import org.apache.commons.lang3.exception.ExceptionUtils
import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotation._
import scala.collection.JavaConversions._
import org.apache.spark.rdd.RDD

import java.util.Properties

import org.apache.spark.SparkContext._
import org.apache.spark.{SparkConf,SparkContext}

/**
 * A class that distributes teh stemming process. Several parameters are available for tuning including the max result size, executor max memory,
 * and driver max memory (15g,8g,4g respectively by default).  A hadoop directory should first be provided.
 */
class DistributedStemmer(hadoopDir:String = "C:\\Users/packe/Documents/hadoop-2.7.1",mRS:String ="15g",emx:String = "8g",dmx:String="4g"){
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
  def stem(texts:List[String]):RDD[Seq[String]]={
    val parts = sc.parallelize(texts)
    val props = new Properties()
    props.put("annotators", "tokenize, ssplit, pos, lemma")
    val pipeline = new StanfordCoreNLP(props)
    
    val lemmatized = parts.mapPartitions( part =>
      part.map { Stemmer.plaintTextToLemmas(_,StopWords.stopList.toSet, pipeline)}
    )
    
    lemmatized
  }
}