package com.simplrtek.vectorizers

import breeze.linalg.{DenseMatrix,DenseVector}

import java.io.File
import com.simplrtek.pickle.Pickler

class WordCountVectorizer(cols:Integer,rows:Integer) extends Vectorizer[String,Double] with Serializable{
  var matrix:DenseMatrix[Double] = new DenseMatrix[Double](cols,rows)
  var posMap:Map[String,Integer] = Map[String,Integer]() //serves as a lookup table
  
  def transform(text:String):DenseVector[Double]={
    null
  }//vectorize
  
  def fit(vectorees:List[String]):DenseMatrix[Double]={
    null
  }//buildVectors
  
  /**
   * Return the vectorized data in a Dense matrix
   */
  def getVectors():DenseMatrix[Double]={
    matrix
  }
  
  /**
   * Clear the tokenizer
   */
  def clear()={
    matrix = new DenseMatrix[Double](cols,rows)
    posMap=Map[String,Integer]()
  }
  
  /**
   * Save the tokenizer to a file
   * @param		matFile		The matrix file
   * @param		posFile		The position map file
   */
  def save(matFile:File,posFile:File)={
     Pickler.pickleTo(matrix, matFile)
     Pickler.pickleTo(posMap, posFile)
  }
  
  /**
   * Load tokenizer data from files.
   * @param		matFile		The matrix file to load from.
   * @param		posFile		The positional map file to load from.
   */
  def load(matFile:File,posFile:File)={
    matrix = Pickler.unpickleFrom[DenseMatrix[Double]](matFile)
    posMap = Pickler.unpickleFrom[Map[String,Integer]](posFile)
  }
  
}