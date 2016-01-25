package com.simplrtek.vectorizers

import com.simplrtek.enriched.Implicits._

import breeze.linalg.{SparseVector,Vector,CSCMatrix}
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node.
 */
class TFIDFVectorizer {
  
  private var docTotalCount : Vector[Double] =_
  private var totalDocTermCount : Vector[Double] = _
  
  def getMCTF()=Future{
    
  }
  
  def getMCIDF()=Future{
    
  }
  
  private def distributedFit()={
    
  }//distributedFit
  
  def fit(freqMat : CSCMatrix[Double])={
   
   
  }//fit
  
  def transform()={
    
  }//transform
  
  def fit_transform()={
    
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}