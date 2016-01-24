package com.simplrtek.vectorizers

import org.apache.mahout.math.SparseMatrix
import org.apache.mahout.math.SparseMatrix
import org.apache.mahout.sparkbindings._
import com.simplrtek.enriched.Implicits._

/**
 *	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node.
 */
class TFIDFVectorizer {
  
  
  def fit(freqMat : SparseMatrix)={
   var tfidfMat : SparseMatrix = new SparseMatrix(freqMat.numRows(),freqMat.numCols())
   
  }//fit
  
  def transform()={
    
  }//transform
  
  def fit_transform()={
    
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}