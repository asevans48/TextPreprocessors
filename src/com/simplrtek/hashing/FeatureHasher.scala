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
   * Resize the Vector
   * 
   * @param		vect		The original RandomAccessSparseVector
   * @return	A RandomAccessSparseVector with an additional slot
   */
  def resizeVector(vect:RandomAccessSparseVector):RandomAccessSparseVector={
    var v2: RandomAccessSparseVector = new RandomAccessSparseVector(vect.size()+1)
    v2.assign(vect)
    v2
  }
  
  def fit()={
    
  }
  
  
  def resizeList(arr:List[Double],cap:Integer):List[Double]={
    var arr2:List[Double] = List.fill(cap)(0.0)
    
    for(i <- 0 to arr.size){
      arr2.updated(i, arr(i))
    }
    
    arr2
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
  def transform(counts:List[Map[String,Integer]],features:Integer = 10000)={
    var vptrs: List[Integer] = List[Integer]()
    var indices:List[Integer] = List[Integer]()
    var values: List[Double] = List.fill(features)(0.0)
    var feats:Integer = features
    var size:Integer = 0
    
    for(map <- counts){
      for(tup <- map){
        var value = tup._2
        if(value > 0){
          var hash = Hash.murmurHashString(tup._1)
          var index = hash % feats
          indices = indices :+ index.asInstanceOf[Integer]
          
          if(hash < 0){
            value *= -1
          }
          
          values.updated(size, value)
          
          size += 1
          
          if(size == feats){
            feats *= 2
            values = resizeList(values,feats)    
          }
          
        }
      }
      vptrs = vptrs :+ size
    }
  }
}