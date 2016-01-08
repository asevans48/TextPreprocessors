package com.simplrtek.vectorizers

import breeze.linalg.{DenseMatrix,DenseVector}

import com.simplrtek.preprocessors.WordTokenizer

import java.io.File
import com.simplrtek.pickle.Pickler

import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class WordCountVectorizer{

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
}