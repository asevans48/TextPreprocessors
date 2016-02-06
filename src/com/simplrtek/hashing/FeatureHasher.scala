package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer
import breeze.linalg._
import breeze.linalg.CSCMatrix
import com.simplrtek.hashing.Hash
import com.simplrtek.enriched.Implicits._

import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * The parallel Feature Hasher creates a more memory intensive version 
 * of a hasher that must be matched with an appropriately parallel TFIDF
 * or other vectorizer. 
 * 
 * The values are Array[Array[(Int,Double)]] representing rows with sparse
 * index to value mappings. This contrasts to 3 large arrays representing
 * values, indices, and row pointers
 * 
 * 
 */
class ParallelFeatureHasher(features : Integer = 500000){
  
}

/**
 * This class contains feature hashing capabilities. Capabilities includes the 
 * use of a basic feature hashing task using multiple arrays to then build a Sparse Matrix. 
 * It also includes a fork join pool version that can potentially reduce the memory footprint
 * and improve speed. It is not  a word count vectorizer. There is a separate class for this. 
 * 
 * 
 * References:
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/feature_extraction/_hashing.py
 * 
 * Hashes Text Features to matrices.
 */
class FeatureHasher(features :Integer = 500000){
  var vptrs: List[Integer] = List[Integer]()
  var indices:List[Integer] = List[Integer]()
  var values: List[Double] = List.fill(features)(0.0)
  var ndocs : Int = 0
  var mx : Int = 0
  
  /**
   * Resizes the list. 
   * 
   * @param		arr		The list to resize
   * @param		cap		The new cap
   * @return	The resized array.
   */
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
   * @param		partial										The boolean stating whether to only partially transform the document.
   * @return	A Sparse Matrix
   */
  def transform(counts:List[Map[String,Integer]],partial : Boolean = false)={
    var feats:Integer = features
    var size:Integer = 0
    
    if(partial == false){
      vptrs = List[Integer]()
      indices = List[Integer]()
      values = List.fill(features)(0.0)
      ndocs = counts.size
      mx = 0
    }else{
      ndocs += counts.size
    }
    
    for(map <- counts){
      for(tup <- map){
        var value = tup._2
        if(value > 0){
          var hash = Hash.murmurHashString(tup._1)
          var index = Math.abs(hash) % feats
          indices = indices :+ index.asInstanceOf[Integer]
          mx = Math.max(mx,index)
          if(hash < 0){
            value *= -1
          }
          
          values = values.updated(size, value.doubleValue())
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
  
  def getCSCMatrix()={
    
    if(vptrs.size ==0){
      throw new Exception("Count maps must have data. Size of row pointers is 0")
    }

    
    val sm = new CSCMatrix.Builder[Double](mx,ndocs)
    var start:Integer = 0
    for(i <- 0 until vptrs.size){
       while(start < vptrs(i)){
         sm.add(indices(start),i, values(start))
         start += 1
       }
    }
    sm.result
  }
}