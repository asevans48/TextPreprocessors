package com.simplrtek.vectorizers

import org.apache.mahout.math.SparseMatrix
import org.apache.mahout.math.Vector
import com.simplrtek.enriched.Implicits._
import org.apache.mahout.math.scalabindings._
import org.apache.mahout.sparkbindings._

import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node.
 */
class TFIDFVectorizer {
  
  private var docTotalCount : Vector =_
  private var totalDocTermCount : Vector = _
  
  def getMCTF()=Future{
    
  }
  
  def getMCIDF()=Future{
    
  }
  
  private def distributedFit()={
    
  }//distributedFit
  
  def fit(freqMat : SparseMatrix)={
   var tfidfMat : SparseMatrix = new SparseMatrix(freqMat.numRows(),freqMat.numCols())
   docTotalCount = freqMat.getColumnNNZ()
   totalDocTermCount = freqMat.getRowNNZ()
   
   freqMat.times(.5)
   
   
  }//fit
  
  def transform()={
    
  }//transform
  
  def fit_transform()={
    
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}