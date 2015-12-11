package com.simplrtek.vectorizers

import breeze.linalg.{DenseMatrix,DenseVector}

/**
 * A transformer inspired by Sci-Kit Learns Vectorizer. This transformer is a bit more rigid in that it is not weak typed but generically typed.
 * The input type and output type should be specified [input,output]
 * 
 */
abstract class Transformer[K,V]{
  
  /**
   * The matrix containing the relevant data.
   */
  var matrix:DenseMatrix[V]
  
  /**
   * Fit vectors without actually transforming them.
   * @param		vectorees		The DenseMatrix to fit
   */
  def fit(vectorees:DenseMatrix[K])
  
  /**
   * Fit and transform vectors, returning the transformed Dense Matrix
   * @param		vectorees		The Dense Matrix to Fit and transform.
   * @return	A transformed Dense Matrix
   */
  def fit_transform(vectorees:DenseMatrix[K]):DenseMatrix[V]
  
  /**
   * Transform a vector using the matrix.
   * 
   * @param		vectoree		The Dense Vector to transform
   * @retun		The dense vectors to be fit into the system.
   */
  def transform(vectoree:DenseVector[K]):DenseVector[V]
  
}