package com.simplrtek.Driver

import breeze.linalg.operators.CSCMatrixOps
import breeze.linalg._
import breeze.optimize._
import breeze.numerics._

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
    val parallelHasher :FeatureHasher = new FeatureHasher()
    var wc : WordCountVectorizer = new  WordCountVectorizer()
     
     //val counts = wc.fit(List("apple apple green red eat good full."))
    
     var testLines = List[String]()
      var testSet = List("its just natural. it doesnt have to be forever though at this point im completely gone","weird thing is I just liked how she looked until i started talking to her. that, I can manage but the rest. shit. modarchod.","why cant I get through to this one?","there would be a hole in my heart if she left work","i could look into those blue eyes for days on end","i like that she likes interesting things","shes sweet and yet calculated","sometimes she puts her hair behind her ears a bit or lets it out a little and i get sent to the moon for days","i have not felt like this about a girl in ten years","shes even has a photographic eye with great attention to detail","I like her smile and her eyes can pierce your soul","best of all, she isnt stupid","i think i have a crush","if i could marry that woman, i would","unlike other women who are as good looking as she is, she is not a bitch","she looks 25","she is a great cook","she has so many awesome interests","cheryl gansel is an amazing woman","hippies. smelly hippies.","no kitty thats my pot pie","screw you guys I am a going home","test this","apple fish green red eat good full yum fish. one fish two fish red fish blue fish.","this is a string.","this is a test, test, test.","I am sentence one. I am sentence two")
     var testString = testSet((Math.random() * testSet.size).asInstanceOf[Int])
     for(i <- 0 to 5000){
       testString += testSet((Math.random() * testSet.size).asInstanceOf[Int])
     }
    println("Starting")
    testLines=SentTokenizer.getSentencesFromRegex(testString)
    println(testLines.size)
    var t = System.currentTimeMillis()
    var counts = wc.fit(testLines)
    parallelHasher.transform(counts)
    var tfpar : TFIDFVectorizer = new TFIDFVectorizer(parallelHasher)
    tfpar.transform()
    println(System.currentTimeMillis() - t)
    println("Getting CSC")
    wc = null
    counts = null
    System.gc()
    Runtime.getRuntime.gc()
    
    val csc = tfpar.getCSCMatrix()
    //println(csc)
    tfpar = null
    System.gc()
    Runtime.getRuntime.gc()
    println(csc(0 until csc.rows,0).activeIterator.toList.filter(_._2 > 0.0))
    println("Calc Cosines")
    val cos =  csc.t * csc / (Math.pow(csc.activeValuesIterator.map { x => x * x }.sum,2))
    println(cos)
  }
}