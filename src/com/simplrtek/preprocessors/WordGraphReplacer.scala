package com.simplrtek.preprocessors

import scala.io.Source
import scala.concurrent.{Future,Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.ConcurrentHashMap


import com.simplrtek.vectorizers.WordCountVectorizer

import sbt.io.IO
import java.io.File

import com.simplrtek.pickle.Pickler

import net.didion.jwnl.data.Synset
import com.simplrtek.similarity.ICDisambiguation

/**
 * Replace words in an object and create a replacement model. This is not distributable because of the iterative nature of building a replacement model.
 * However, concurrency is used where possible.
 */
class WordGraphReplacer(rows : Integer = 100, cols : Integer = 100,cosCutoff : Double = 0.6){
  val dis = new ICDisambiguation()
  var synMap : Map[Synset,String] = Map[Synset,String]()
  
  
  def replaceWord()={
    
  }//replaceWord
  
  
  def replace(text : String)={
    
  }//replace
  
  
  def replaceFile( f: File)={
     val sents = SentTokenizer.getSentencesFromFile(f)
     sents.foreach { x => replace(x) }
  }//replaceFile
  
}

object TestSDriver{
  def main(args:Array[String])={

  }
}