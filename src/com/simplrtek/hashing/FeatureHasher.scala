package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer
import org.apache.mahout.math.{SparseRowMatrix,SparseMatrix}
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector}
import com.simplrtek.hashing.Hash
import com.simplrtek.enriched.Implicits._

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
  
  def fit()={
    
  }
  
  /**
   * Converts a List of String,Count Pairs to a Sparse Matrix
   * using the hashing trick. Uses Mahout and the Implicits to
   * build the matrix. The algorithm uses the Hashing Trick.
   * 
   * @param		counts										A list of document counts
   * @param		{Integer}{features}				The minimum features size
   * @return	A Sparse Matrix
   */
  def transform(counts:List[Map[String,Integer]],features:Integer = 10000):SparseMatrix={
    var data = new RandomAccessSparseVector(features)
    var indices = new RandomAccessSparseVector(features)
    var currIndex: Integer = 0
    
    for(map <- counts){
      for(wtup <- map){
        if(wtup._2 != 0){
          val h = Hash.murmurHashString(wtup._1)
          var value:Double = wtup._2.asInstanceOf[Double] 
          if(!(h >= 0)){
            value *= - 1
          }
          data.set(currIndex, value)
          
          currIndex += 1
          
          if(currIndex == features){
            
          }
        }
      }
    }
    
    
    //create a dense matrix from the list of arrays
    null
  }
}