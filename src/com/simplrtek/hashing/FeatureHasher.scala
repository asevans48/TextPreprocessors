package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer
import breeze.linalg.{DenseVector,DenseMatrix}
import breeze.linalg.{CSCMatrix,SparseVector}
/**
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/feature_extraction/_hashing.pyx
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/utils/murmurhash.pyx
 * 
 * Hashes Text Features to matrices.
 */
class FeatureHasher{
  
  /**
   * If there are more than 10000 features a resize is necessary.
   */
  def resizeVector(arr:Array[Integer],size:Integer):Array[Integer]={
    var newArr:Array[Integer] = Array.fill[Integer](arr.size + 1)(0)
    Array.copy(arr, 0, newArr, 0, arr.length)
    newArr
  }
  
  /**
   * Converts a List of String,Count Pairs to a CSC Matrix
   * using the hashing trick.
   */
  def transform(counts:List[Map[String,Integer]],features:Integer = 10000):CSCMatrix[Integer]={
    //TODO Build out the transformer using Mahout or Breeze
    
    
    //create a dense matrix from the list of arrays
    null
  }
}