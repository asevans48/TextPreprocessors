package com.simplrtek.vectorizers

import com.simplrtek.enriched.breeze.Implicits._
import breeze.linalg.{CSCMatrix,Matrix}
import breeze.linalg.max
import breeze.numerics._
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.VectorBuilder
import scala.util.{Success,Failure}
/**
 *	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node.
 */
class TFIDFVectorizer {
  
  private var docTermCount : Vector[Double] = _
  private var maxDocFreqs : Vector[Double] = _ 
  
  def getMax(mat : Matrix[Double]):Future[Double]=Future{
    mat.activeValuesIterator.max
  }
  
  
  def getMaxFreqs(freqMat : CSCMatrix[Double],batchSize : Int  = 100, duration : Duration = Duration.Inf)={
     val nrows : Int = freqMat.rows
     val ncols : Int = freqMat.cols
     val builder = Vector.newBuilder[Double]
     var matrices : List[Matrix[Double]] = List[Matrix[Double]]()
     var start : Int = 0
     var end : Int = batchSize
     
     while(start < freqMat.cols){
     
       for(i <- start until end){
          matrices = matrices :+ freqMat(i to i, 0 until freqMat.cols)
       }
       
       val r = Await.ready(Future.traverse(matrices)(getMax), duration)
       r.value.get match{
         case Success(t) =>{
           t.foreach { 
             d =>
              builder += d 
           }
         }
         case Failure(e) => {
            println("Failed to get Results from Future, Failure!")
            System.exit(-1)
         }
       }
       
       start += batchSize 
     }
     
     maxDocFreqs = builder.result
    
  }
  
  
  def getIDFMat(freqMats : CSCMatrix[Double])={
    for(i <- 0 until freqMats.cols){
      
    }
  }
  
  def tfCalculator(freqs : Matrix[Double], max : Double):Future[Double]=Future{
    0
  }
  
  def getTFMat(freqMats : CSCMatrix[Double],maxFreqs : Vector[Double])={
    val builder = Vector.newBuilder[Double] 
    
  }
  
  def fit(freqMat : CSCMatrix[Double])={
   
   
  }//fit
  
  def transform()={
    
  }//transform
  
  def fit_transform()={
    
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}