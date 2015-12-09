package com.simplrtek.preprocessors

import org.apache.commons.lang3.exception.ExceptionUtils

import java.io.File
import sbt.io.IO

import com.simplrtek.preprocessors.{Stemmer,NumericConverter,NamedEntity,SentTokenizer}

import scala.concurrent.{Await,Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import sbt.io.IO

/**
 * Pre-process text in preparation for generating a TFIDF matrix.
 */
class PreprocessText {
  
  def tokenize(file:java.io.File,binFile:String = "data/models/en-token.bin"):Future[String]=Future{
    SentTokenizer.load(binFile)
    SentTokenizer.getSentencesFromFile(file, binFile).mkString("\n")
  }//tokenize
  
  def replaceNames(text:String,jarPath:String = "data/models/epic-ner-en-conll_2.10-2015.2.19.jar"):Future[String]=Future{
    NamedEntity.load(jarPath)
    NamedEntity.replaceEntities(text).mkString("\n")
  }//replaceNames
  
  def replaceNumerics(text:String):Future[String]=Future{
    NumericConverter.replace(text)
  }//replaceNumerics
  
  
  def stem(text:String):Future[String]=Future{
    Stemmer.lemmatize(text).mkString(" ")
  }//stem
  
  
  
  def processFiles(directory:String,outDirectory:String,flow:List[String],binFile:String="data/models/en-token.bin",waitTime:Duration = Duration.Inf)={
    val files=IO.listFiles(new java.io.File(directory)).toList
    val futs:List[Future[String]] = files.map {
      f => {
        var fut:Future[String] = null
        if(flow.contains("tokenize")){
          fut=tokenize(f,binFile)
        }
        
        if(flow.contains("ner")){
          if(fut == null){
            fut=replaceNames(IO.read(f))
          }else{
            fut = fut.flatMap(replaceNames(_))
          }
        }
        
        if(flow.contains("stem")){
          if(fut == null){
            fut=stem(IO.read(f))
          }else{
            fut = fut.flatMap{stem(_)}
          }
        }
        
        if(flow.contains("numeric")){
          if(fut == null){
            fut = replaceNumerics(IO.read(f))
          }else{
            fut = fut.flatMap { replaceNumerics(_)}
          }
        }
        
      }
    }.asInstanceOf[List[Future[String]]]
    
    val res = Await.result(Future.sequence(futs), waitTime)
    for(i <- 0 to res.size){
      IO.write(new java.io.File(outDirectory,i+".txt"), res(i).getBytes)  
    }
    
  }//processFiles
}

object PrepDriver{
  def main(args:Array[String])={
    //processFiles("data/)
  }
}