package com.simplrtek.Driver
import com.simplrtek.enriched.Implicits._
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}

import org.scalatest._
import org.scalatest.Matchers._
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import com.simplrtek.math.Smoother
import java.lang.ArrayIndexOutOfBoundsException
import com.simplrtek.vectorizers._
import com.simplrtek.hashing._
import sbt.io

import com.simplrtek.preprocessors.SentTokenizer

import com.simplrtek.hashing.FeatureHasher
import com.simplrtek.vectorizers.WordCountVectorizer
import scala.collection.immutable.StringOps
import com.simplrtek.vectorizers.TFIDFVectorizer

object HTestDriver{
  
  def main(args:Array[String]):Unit={
    val hasher : FeatureHasher = new FeatureHasher()
    val wc : WordCountVectorizer = new  WordCountVectorizer()
     
     //val counts = wc.fit(List("apple apple green red eat good full."))
     var testLines = List[String]()
     var testString ="this is a string."
     for(i <- 0 to 100){
       testString += "apple fish green red eat good full yum fish. one fish two fish red fish blue fish."
     }
    println("Starting")
    testLines=SentTokenizer.getSentencesFromRegex(testString)
   
     var t = System.currentTimeMillis()
     val counts = wc.fit(testLines)
     hasher.transform(counts)
     println(System.currentTimeMillis()-t)
     t= System.currentTimeMillis()
     val tfidf = new TFIDFVectorizer(hasher)
     tfidf.transform()
     
     println(System.currentTimeMillis() - t)
     println(tfidf.getCSCMatrix())
  }
}