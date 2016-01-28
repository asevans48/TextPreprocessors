package com.simplrtek.preprocessors

import scala.io.Source
import scala.concurrent.{Future,Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.ConcurrentHashMap

import com.simplrtek.vectorizers.WordCountVectorizer

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import sbt.io.IO
import java.io.File

import com.simplrtek.pickle.Pickler

import net.didion.jwnl.data.Synset

/**
 * Replace words in an object and create a replacement model. This is not distributable because of the iterative nature of building a replacement model.
 * However, concurrency is used where possible.
 */
class WordGraphReplacer(rows : Integer = 100, cols : Integer = 100,cosCutoff : Double = 0.6){
  
  var synMap : Map[Synset,String] = Map[Synset,String]()
  
  
  def replaceWord()={
    
  }//replaceWord
  
  
  def replace(text : String)={
    
  }//replace
  
  
  def replaceFile( f: File)={
     val sents = SentTokenizer.getSentencesFromFile(f)
     
  }//replaceFile
  
}

object TestSDriver{
  def main(args:Array[String])={

  }
}