package com.simplrtek.vectorizers

import breeze.linalg.{DenseVector,DenseMatrix}
import java.io.File

/**
 * This trait standardizes the building of vectorizers. A vectorizer
 * should take in any list of data and be able to convert it to a 
 * vector. The data in the Vector is highly likely to differ from
 * that of the input value. Therefore, two generics are provided. 
 * The first is the input data type and the second is the Output type.
 * 
 * While a matrix object and position map are required, the vectorizer should also contain 
 * any necessary count objects as well.
 */
abstract class Vectorizer[T,E]{
  
  /**
   * The dense matrix containing all vectors.
   */
  var matrix: DenseMatrix[E]
  
  /**
   * The map of the 
   */
  var posMap: Map[T,Integer]
  
  /**
   * Intended for performing vectorization. While the vector is returned it is also added to a Dense Matrix.
   * @param		vectoree		The data to vectorize
   * @return	A Dense Vector containing data of type E.
   */
  def transform(vectoree:T):DenseVector[E]
  
  
  /**
   * Get the dense matrix of type E.
   * @return	The dense matrix.
   */
  def getVectors():DenseMatrix[E]
  
  /**
   * Clear All values from the vectorizer.
   */
  def clear()
  
  /**
   * Save the tokenizer to a file
   * @param		matFile		The matrix file
   * @parma		posFile		The file for the position map
   */
  def save(matFile:File,posFile:File)
  
  
  /**
   * Load a tokenizer
   * @param		matFile		The matrix File
   * @param		posFile		The positionMap File
   */
  def load(matFile:File,posFile:File)
}