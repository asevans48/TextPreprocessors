package com.simplrtek.preprocessors

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


/**
 * Replace words in an object and create a replacement model. This is not distributable because of the iterative nature of building a replacement model.
 * However, concurrency is used where possible.
 */
class WordGraphReplacer(cols:Integer = 100, rows:Integer = 100){
  var tokenizer:WordCountVectorizer = new WordCountVectorizer() //for tokenizing a sentence to a vector (must be built first)
  var replacementMap:ConcurrentHashMap[String,String] = new ConcurrentHashMap[String,String]()
  
  /**
   * The first step in the algorithm. Builds a tokenizer to create vectors from sentences.
   * 
   * @param		directory		The directory to use in building the tokenizer.
   */
  def buildTokenizer(directory:File)={
    
  }
  
  /**
   * Load a tokenizer.
   * 
   * @param		tokenizerPath		The java.io.File where the path is.
   */
  def loadTokenizer(tokenizerPath:File)={
    tokenizer = Pickler.unpickleFrom[WordCountVectorizer](tokenizerPath)
  }
  
  
  /**
   * Save the tokenizer
   */
  def saveTokenizer(file:File)={
     Pickler.pickleTo(tokenizer, file)
  }
  
  /**
   * Save the model.
   */
  def saveModel(file:File)={
    Pickler.pickleTo(replacementMap, file)
  }
  
  /**
   * Load a model.
   */
  def loadModel(modelPath:File)={
    replacementMap = Pickler.unpickleFrom[ConcurrentHashMap[String,String]](modelPath)
  }
  
  /**
   * Take in a string and perform replacement.
   * 
   * @param		text									The string to replace words from
   * @param		cosCutoff							The cosine cutoff to consider in whether to replace a word (based on best definition)
   * @param		{String}{binFile}			The model to use in sentence tokenization
   * @param		{Duration}{termTime]	The durationt to wait for replacement to finish.
   * @param		{File}{tokenizerPath}	The path where the tokenizer is. Deafult is null which expects a loaded tokenizer.
   * @return	A List of List of words representing sentences void of punctuation
   */
  def replaceWords(text:String,cosCutoff:Double = .9,binFile:String = "data/models/en-token.bin",termTime:Duration = Duration.Inf,tokenizerPath:File = null):List[List[String]]={
    
   
    
    /**
     * Replace the words in a sentence
     * 
     * @param		sentence		The sentence to replace words in.
     */
    def replaceWords(sentence:String):Future[List[String]]=Future{
      
      def getDefinition(words:List[String],word:String):List[String]={
        null
      }//get the definition using cosines
      
      def checkDefs()={
        
      }//check definitions against existing defs using a cosine cutoff value
      
     
      
      var words = WordTokenizer.wordTokenize(PunctReplacer.replacePunct(text))
      words = words.filter({ word => !StopWords.stopList.contains(word)})
      
      
      null
    }
    
    SentTokenizer.load(binFile)
    Await.result(Future.traverse(SentTokenizer.getSentences(text).toList)(replaceWords(_)),termTime)
  }
  
  
  /**
   * The function that performs the actual
   *
   * @param		directory		The directory where the files reside.
   * @return	A list of list of strings representing sentences with certain words replaced.
   */
  def replace(directory:File):List[List[String]]={
    var sentences:List[List[String]] = List[List[String]]()
    buildTokenizer(directory)
    IO.listFiles(directory).foreach { 
      file =>
        var text = IO.read(file)
        sentences = sentences ++ replaceWords(text)
     }
    sentences
    
  }
}

object TestSDriver{
  def main(args:Array[String])={

  }
}