package com.simplrtek.vectorizers

import breeze.linalg.{DenseMatrix,DenseVector}

import com.simplrtek.preprocessors.WordTokenizer

import java.io.File
import com.simplrtek.pickle.Pickler

import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class WordCountVectorizer(cols:Integer,rows:Integer) extends Serializable{
  var matrix:DenseMatrix[Double] = new DenseMatrix[Double](cols,rows)
  var posMap:Map[String,Integer] = Map[String,Integer]() //serves as a lookup table
  
  def transform(text:String):Future[Map[String,Integer]]=Future{
    var map:Map[String,Integer] = Map[String,Integer]()
    var words = WordTokenizer.wordTokenize(text)
    
    for(word <- words){
      if(map.contains(word)){
        map.updated(word, map.get(word).get + 1)
      }else{
        map = map + (word -> 1)
      }
    }
    
    //get counts
    map
  }
  
  def fit(texts:List[String],duration:Duration = Duration.Inf):List[Map[String,Integer]]={
    Await.result(Future.traverse(texts)(transform(_)),duration)
  }
  
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