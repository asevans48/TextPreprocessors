package com.simplrtek.Driver

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
import com.simplrtek.hashing.ParallelFeatureHasher
import com.simplrtek.vectorizers.WordCountVectorizer
import scala.collection.immutable.StringOps
import com.simplrtek.vectorizers.TFIDFVectorizer
import com.simplrtek.vectorizers.ParallelTFIDFVectorizer

object HTestDriver{
  
  def main(args:Array[String]):Unit={
    val hasher : FeatureHasher = new FeatureHasher()
    val parallelHasher :ParallelFeatureHasher = new ParallelFeatureHasher()
    val wc : WordCountVectorizer = new  WordCountVectorizer()
     
     //val counts = wc.fit(List("apple apple green red eat good full."))
     var testLines = List[String]()
     var testString ="this is a string."
     for(i <- 0 to 10){
       testString += "apple fish green red eat good full yum fish. one fish two fish red fish blue fish."
     }
    println("Starting")
    testLines=SentTokenizer.getSentencesFromRegex(testString)
    var t = System.currentTimeMillis()
    val counts = wc.fit(testLines)
    parallelHasher.transform(counts)
    //println(parallelHasher.getCSCMatrix())
    val tfpar : ParallelTFIDFVectorizer = new ParallelTFIDFVectorizer(parallelHasher)
    tfpar.transform()
    println(System.currentTimeMillis() - t)
    tfpar.getCSCMatrix()
    //println(tfpar.getCSCMatrix())
    /**
    testLines=SentTokenizer.getSentencesFromRegex(testString)
     var t = System.currentTimeMillis()
     val counts = wc.fit(testLines)
     hasher.transform(counts)
     println(System.currentTimeMillis()-t)
     t= System.currentTimeMillis()
     val tfidf = new TFIDFVectorizer(hasher)
    println("ROWS: "+hasher.getCSCMatrix().cols) 
    tfidf.transform()
     
     println(System.currentTimeMillis() - t)
     println(tfidf.getCSCMatrix())
     * 
     */
  }
}