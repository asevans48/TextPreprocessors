package com.simplrtek.vectorizers

import com.simplrtek.enriched.breeze.Implicits._
import breeze.linalg.{CSCMatrix,Matrix}
import breeze.linalg.max
import breeze.linalg.*
import breeze.numerics._
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.VectorBuilder
import scala.util.{Success,Failure}
import com.simplrtek.hashing.FeatureHasher
import com.simplrtek.hashing.ParallelFeatureHasher

import org.apache.commons.lang3.exception.ExceptionUtils

/**
 * Uses a parallel hasher to obtain a TFIDF Matrix.
 * The hasher array will be transformed.
 */
class ParallelTFIDFVectorizer(hashClass: ParallelFeatureHasher,batchSize : Int = 100, duration : Duration = Duration.Inf){
  private var docTermCounts : Map[Int,Int] = Map[Int,Int]()
  private var maxDocFreqs : List[Double] = _
  var hasher : ParallelFeatureHasher = hashClass
  
  /**
   * Gets the maximum document frequency for an array of counts.
   */
  def getMaxDocFreqs(counts : Array[(Int,Double)] ):Future[Double]=Future{
     counts.maxBy(f => f._2)._2
  }
  
  /**
   * Gets the number of non-zero elements.
   */
  def getDocTermCounts(counts : List[Double],totalDocs : Int):Future[Double]=Future{
    counts.map { x => if(x > 0) 1 else 0 }.sum / totalDocs
  }
  
  
  /**
   * Calculate the rows TF values
   */
  def calcTF( counts : Array[(Int,Double)], maxFreq : Double):Future[Array[(Int,Double)]]=Future{
    counts.map({x => (x._1,0.5 + 0.5 * (x._2/maxFreq))})
  }
  
  /**
   * Calculate the TFIDF
   */
  def calcTFIDF(row : Array[(Int,Double)]):Future[Array[(Int,Double)]]=Future{
    var newArr : Array[(Int,Double)] = Array.fill(row.length)(null)
    for(i <- 0 until row.length){
      newArr.update(i, (row(i)._1,row(i)._2/this.docTermCounts(i)))
    }
    newArr
  }
  
  /**
   * Calculate the TF part of TFIDF
   */
  def getTF()={
    var futs : List[Future[Double]] = List[Future[Double]]() 
    this.maxDocFreqs = List[Double]()
    
    //calculate max doc freqs
    for(i <- 0 until this.hasher.cmr.length){
        futs = futs :+ this.getMaxDocFreqs(this.hasher.cmr(i))
        
        if(futs.size == this.batchSize || i + 1 == this.hasher.cmr.length){
           val r = Await.ready(Future.sequence(futs), duration).value.get
           r match{
             case Success(x) =>{
               x.foreach { v => 
                 this.maxDocFreqs = this.maxDocFreqs :+ v 
               }
               futs = List[Future[Double]]()
             }
             case Failure(t) =>{
               println("Failed to Get Max Doc Freqs!\n"+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
               System.exit(-1)
             }
           }
        }
    }
    futs = null
    
    //calc the tf
    var tfFuts : List[Future[Array[(Int,Double)]]] = List[Future[Array[(Int,Double)]]]()
    var j : Int = 0
    for(i <- 0 until this.hasher.cmr.length){
      tfFuts = tfFuts :+ this.calcTF(this.hasher.cmr(i),this.maxDocFreqs(i))
      
      if(tfFuts.size == this.batchSize || (i + 1) == this.hasher.cmr.length){
        val r = Await.ready(Future.sequence(tfFuts), duration).value.get
        r match{
          case Success(x) =>{
            x.foreach({
              r =>
                this.hasher.cmr.update(j, r)
                j += 1
            })
           }
          case Failure(t) =>{
            println("Failed to Calculate TF!\n"+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
            System.exit(-1)
          }
        }
        
      }
    }
  }
  
  /**
   * Calculate the document term counts part of the IDF
   */
  def getDocTermCounts()={
    //get the IDF value   
    for(i <- 0 until this.hasher.cmr.length){
       for(j <- 0 until this.hasher.cmr(i).length){
        if(this.docTermCounts.contains(this.hasher.cmr(i)(j)._1)){
          this.docTermCounts = this.docTermCounts.updated(this.hasher.cmr(i)(j)._1, this.hasher.cmr(i)(j)._2.asInstanceOf[Int] + 1)
        }else{
          this.docTermCounts = this.docTermCounts +  (this.hasher.cmr(i)(j)._1 -> 1)
        }
       }
    }
  }
  
  /**
   * Calculates the TFIDF value via multiplication
   */
  def getTFIDF()={
    for(i <- 0 until this.hasher.cmr.length){
      for(j <- 0 until this.hasher.cmr(i).length){
         this.hasher.cmr(i)(j) = (this.hasher.cmr(i)(j)._1,Math.log(this.hasher.ndocs / this.docTermCounts(this.hasher.cmr(i)(j)._1)) * this.hasher.cmr(i)(j)._2) 
      }
    }
  }
  
  
  /**
   * Control the Transform
   */
  def transform()={
    this.getDocTermCounts()
    this.getTF()
    this.getTFIDF()
  }
  
  /**
   * Get a CSCMatrix from the new hasher values
   */
  def getCSCMatrix():CSCMatrix[Double]={
    this.hasher.getCSCMatrix
  }
  
}

/**
 * This class takes in the hash arrays from the FeatureHasher
 * class and uses the lists to build a tfidf matrix. This 
 * would be faster than converting to a breeze matrix first.
 */
class TFIDFVectorizer(hasher: FeatureHasher,batchSize : Int = 100, duration : Duration = Duration.Inf){
  private var docTermCount : List[Double] =_
  private var maxDocFreqs : List[Double] = _
  
  def getMaxDocFreqs()={
    
  }
  
  def getDocTerms()={
    
  }
  
  def getTF()={
    
  }
  
  def getIDF()={
    
  }
  
  def transform()={
    
  }
  
  def getCSCMatrix()={
    
  }
}
