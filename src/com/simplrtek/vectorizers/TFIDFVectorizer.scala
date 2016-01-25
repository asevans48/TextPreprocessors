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
/**
 *	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node. 
 * 
 * The constructor takes params.
 * 
 * @param		{Int}{batchSize}									The batchsize to use in multi-core processing
 * @param		{Duration}{duration}							The duration to await results
 */
class TFIDFVectorizer(batchSize : Int  = 100, duration : Duration = Duration.Inf){
  
  private var docTermCount : Vector[Double] = _
  private var maxDocFreqs : Vector[Double] = _ 
  
  /**
   * Get the maximum count from the iterator.
   */
  def getMax(mat : Matrix[Double]):Future[Double]=Future{
    mat.activeValuesIterator.max
  }
  
  
  /**
   * Get maximum counts for each document to be used in TF calculations. 
   * Uses Futures. Sets maxDocFreqs.
   * 
   * @param		freqMat				The CSC frequeny matrix
   * 
   */
  def getMaxFreqs(freqMat : CSCMatrix[Double])={
     val nrows : Int = freqMat.rows
     val ncols : Int = freqMat.cols
     val builder = Vector.newBuilder[Double]
     var start : Int = 0
     var end : Int = batchSize
     
     while(start < freqMat.cols){
       var matrices : List[Matrix[Double]] = List[Matrix[Double]]()
       if(end > freqMat.rows){
        end = freqMat.rows  
       }
       
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
       end += batchSize
     }
     
     maxDocFreqs = builder.result
    
  }
  
  /**
   * Sum the column wise document counts
   */
  def calculateDocTerms(freqs : Matrix[Double]):Future[Double]=Future{
    freqs.valuesIterator.map { x => if(x > 0) 1 else 0}.sum
  }
  
  /**
   * Gets Document Term Counts. This builds a vector of all documents
   * containing a term, reducing a future calculation time. Sets docTermCount
   * 
   * @param		freqMat				The frequency matrix
   */
  def getDocTermCount(freqMat : CSCMatrix[Double])={
    var builder = Vector.newBuilder[Double]
    var start : Int = 0
    var end : Int = batchSize
    
    while(start  < freqMat.cols){
      var matrices : List[Matrix[Double]] = List[Matrix[Double]]()
      if(end > freqMat.cols){
        end = freqMat.cols  
      }
      
      for(i <- start until end){
         matrices = matrices :+ freqMat(0 until freqMat.rows,i to i)
      }
      
      Await.ready(Future.traverse(matrices)(calculateDocTerms), duration).value.get match{
        case Success(t) =>{
          t.foreach { d => builder += d }
        }
        case Failure(e) =>{
          println("Failure in Futures for DocTermCount. Failure!")
          System.exit(-1)
        }
      }
      start += batchSize
      end += batchSize
    }
    
    docTermCount = builder.result
  }
  
  
  def tfCalculator(freqs : Matrix[Double]):Future[Double]=Future{
    0
  }
  
  /**
   * Uses multi-core processing to perform the tfidf component. Will reset the matrix
   * so must be called after the documents are generated. The frequency matrix is 
   * thus lost to save memory. Uses docTermCounts and maxFreqs as well.
   * 
   * @param			freqMat			The frequency matrix
   */
  def getTFMat(freqMat : CSCMatrix[Double])={
    val builder = Vector.newBuilder[Double] 
    
  }
  
  
  /**
   * Fit and transform the vector.
   */
  def fit_transform()={
    
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}