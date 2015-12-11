package com.simplrtek.vectorizers

import breeze.linalg.{DenseMatrix,DenseVector}

/**
 * For small files on a single node, This class works well. It uses breeze directly to perform TFIDF 
 * conversions from a Word Count Vectorizer.
 * 
 * Distributed versions can easily make use of Sparks TFIDF Converter.  
 * 
 * @see WordCountVectorizer
 */
class TFIDFTransformer(cols:Integer = 100, rows:Integer = 100) extends Transformer[Double,Double] with Serializable{
  /**
   * The matrix containing the relevant data.
   */
  var matrix:DenseMatrix[Double] = new DenseMatrix[Double](rows,cols)
  
  /**
   * Fit vectors without actually transforming them.
   * @param		vectorees		The DenseMatrix to fit
   */
  def fit(vectorees:DenseMatrix[Double])={
    
  }
  
  /**
   * Fit and transform vectors, returning the transformed Dense Matrix
   * @param		vectorees		The Dense Matrix to Fit and transform.
   * @return	A transformed Dense Matrix
   */
  def fit_transform(vectorees:DenseMatrix[Double]):DenseMatrix[Double]={
    null
  }
  
  /**
   * Transform a vector using the matrix.
   * 
   * @param		vectoree		The Dense Vector to transform
   * @retun		The dense vectors to be fit into the system.
   */
  def transform(vectoree:DenseVector[Double]):DenseVector[Double]={
    null
  }
}