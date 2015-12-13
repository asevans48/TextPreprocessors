package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer

/**
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/feature_extraction/_hashing.pyx
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/utils/murmurhash.pyx
 * 
 * Hashes Text Features to matrices.
 */
class FeatureHasher(features:Double = math.pow(2, 20)){
  var nfeatures = features
  
  /**
   * Converts a List of String,Count Pairs to a Dense Matrix
   * using the hashing trick.
   */
  def transform(counts:List[Map[String,Integer]])={
    
  }
}