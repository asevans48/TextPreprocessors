package com.simplrtek.vectorizers

import com.simplrtek.enriched.breeze.Implicits._
import org.apache.mahout.math.SparseRowMatrix
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
  private var docTermCounts : scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map[Int,Int]()
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
    counts.map({x => (x._1,0.5 + 0.5 * (x._2/(maxFreq)))})
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

    for(i <- 0 until this.hasher.cmr.size){
      tfFuts = tfFuts :+ this.calcTF(this.hasher.cmr(i),this.maxDocFreqs(i))
      
      if(tfFuts.size == this.batchSize || (i + 1) == this.hasher.cmr.length){
        val r = Await.ready(Future.sequence(tfFuts), duration).value.get
        r match{
          case Success(x) =>{
            x.foreach({
              row =>
                this.hasher.cmr.update(j, row)
                j += 1
            })
            tfFuts = List[Future[Array[(Int,Double)]]]()
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
          this.docTermCounts.update(this.hasher.cmr(i)(j)._1, this.hasher.cmr(i)(j)._2.toInt + this.docTermCounts.get(this.hasher.cmr(i)(j)._1).get)
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
    for(i <- 0 until this.hasher.cmr.size){
      for(j <- 0 until this.hasher.cmr(i).size){
         this.hasher.cmr(i)(j) = (this.hasher.cmr(i)(j)._1,Math.log(this.hasher.cmr.size / (1.0+this.docTermCounts(this.hasher.cmr(i)(j)._1))) * this.hasher.cmr(i)(j)._2) 
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
  
  /**
   * Get a Mahout Sparse Row Matrix from the values
   * 
   * @return		A Mahout Sparse Row Matrix
   */
  def getMahoutSparseRowMatrix():SparseRowMatrix={
    var smr : SparseRowMatrix = new SparseRowMatrix(this.hasher.cmr.length,this.hasher.mx)
    for(i <- 0 until this.hasher.cmr.length){
      for(j <- 0 until this.hasher.cmr(i).length){
        smr.setQuick(i,this.hasher.cmr(i)(j)._1, this.hasher.cmr(i)(j)._2.asInstanceOf[Double])
      }
    }
    smr
  }
  
}

/**
 * This class takes in the hash arrays from the FeatureHasher
 * class and uses the lists to build a tfidf matrix. This 
 * would be faster than converting to a breeze matrix first.
 */
class TFIDFVectorizer(hasher: FeatureHasher,batchSize : Int = 100, duration : Duration = Duration.Inf){
  private var maxDocFreqs : List[Double] = List[Double]()
  private var idfVals : scala.collection.mutable.Map[Int,Double] = scala.collection.mutable.Map[Int,Double]()
  
  /**
   * Get the maximum document frequency counts. 
   */
  def getMaxDocFreqs()={
     var start : Int = 0 
     for(i <- 0 until this.hasher.vptrs.size){
       var mxFreq : Double = 0
       while(start < this.hasher.vptrs(i)){
         mxFreq = Math.max(this.hasher.values.get(start), mxFreq)
         start += 1
       }
       maxDocFreqs = maxDocFreqs :+ mxFreq
     }
  }
  
  /**
   * Get the document max document frequencies. Generates an IDF Values map.
   * 
   */
  def getDocTerms()={
      for(i <- 0 until this.hasher.values.size){
        if(idfVals.contains(this.hasher.indices(i))){
          this.idfVals.update(this.hasher.indices(i), this.idfVals.get(this.hasher.indices(i)).get + 1)
        }else{
          this.idfVals.put(this.hasher.indices(i), 1)         
        }
      }
      
      for(k <- this.idfVals.keys){
        this.idfVals.update(k, Math.log(this.hasher.vptrs.size / (1 + this.idfVals.get(k).get)))
      }
  }
  
  
  /**
   * Calculate the TFIDF value.
   */
  def getTF()={
    var i = 0
    var vptrMax = this.hasher.vptrs(this.hasher.vptrs.size-1)
    var smr : SparseRowMatrix = new SparseRowMatrix(this.hasher.vptrs.size,this.hasher.mx)
    var index = 0
    while(index < vptrMax){
        if(index == this.hasher.vptrs(i)){
          i += 1
        }
        this.hasher.values.set(index, (0.5+((this.hasher.values.get(index)*0.5)/this.maxDocFreqs(i)))* this.idfVals.get(this.hasher.indices(index)).get)
        index += 1

    }
  }
  
  /**
   * Control the TFIDF transformation
   */
  def transform()={
    this.getMaxDocFreqs()
    this.getDocTerms()
    this.getTF()
  }
  
  /**
   * Return a CSC Matrix from the provided hasher.
   */
  def getCSCMatrix():CSCMatrix[Double]={
    this.hasher.getCSCMatrix
  }
  
  
    /**
   * Get a Mahout Sparse Row Matrix from the values
   * 
   * @return		A Mahout Sparse Row Matrix
   */
  def getMahoutSparseRowMatrix():SparseRowMatrix={
    var i = 0
    var vptrMax = this.hasher.vptrs(this.hasher.vptrs.size-1)
    var smr : SparseRowMatrix = new SparseRowMatrix(this.hasher.vptrs.size,this.hasher.mx)
    var index = 0
    while(index < vptrMax){
      if(index == this.hasher.vptrs(i)){
         i += 1
      }
      
      smr.setQuick(i,this.hasher.indices(index), this.hasher.values.get(index))
      index += 1  
    }
    smr
  }
}