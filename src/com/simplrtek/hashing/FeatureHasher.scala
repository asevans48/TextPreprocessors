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
    var newArr = Array.fill[Integer][size](0)
    Array.copy(arr, 0, newArr, 0, arr.length)
  }
  
  /**
   * Converts a List of String,Count Pairs to a CSC Matrix
   * using the hashing trick.
   */
  def transform(counts:List[Map[String,Integer]],features:Integer = 10000):CSCMatrix[Integer]={
    //TODO Build out the transformer using Mahout or Breeze
    val builder = new CSCMatrix.Builder[Double](rows=10, cols=10)
    
    var size:Integer = 0
    var indexList:List[Array[Integer]] = List[Array[Integer]]()
    var datList:List[Array[Integer]] = List[Array[Integer]]()
    for(sentence <- counts){
      var indices:Array[Integer]= Array[Integer]()
      var values:Array[Integer] = Array.fill[Integer][features](0) 
      
      for(key <- sentence.keys){
        val v = sentence.get(key).get
        val h = Hash.murmurHashString(key)
        
        if(indices.length > features){
          indices = resizeVector(indices,indices.length + 1)
        }
        
        indices(indices.length -1) = Math.abs(h) % features
        v = v * ((h*2) -1)
        values(size) = v
        
        size += 1
        
        if(size > values.length){
          values=resizeVector(values,(values.length *2))
        }
        
      }
      
      indexList = indexList :+ indices
      datList = datList :+ values
    }
    //create a dense matrix from the list of arrays
    
  }
}