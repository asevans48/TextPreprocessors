package com.simplrtek.vectorizers

import com.simplrtek.enriched.breeze.Implicits._
import breeze.linalg.{CSCMatrix,Matrix}
import breeze.linalg.max
import breeze.linalg.*
import breeze.numerics._
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.VectorBuilder
import scala.util.{Success,Failure}
import com.simplrtek.hashing.FeatureHasher

/**
 * Uses a parallel hasher to obtain a TFIDF Matrix
 */
class ParallelTFIDFVectorizer(batchSize : Int = 100, duration : Duration = Duration.Inf){
  private var docTermCount : List[Double] =_
  private var maxDocFreqs : List[Double] = _
}

/**
 * This class takes in the hash arrays from the FeatureHasher
 * class and uses the lists to build a tfidf matrix. This 
 * would be faster than converting to a breeze matrix first.
 */
class TFIDFVectorizer(batchSize : Int = 100, duration : Duration = Duration.Inf){
  private var docTermCount : List[Double] =_
  private var maxDocFreqs : List[Double] = _
}
