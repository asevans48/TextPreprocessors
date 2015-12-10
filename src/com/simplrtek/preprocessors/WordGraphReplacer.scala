package com.simplrtek.preprocessors

import scala.concurrent.{Future,Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

/**
 * Replace words in an object and create a replacement model.
 */
object WordGraphReplacer {
  
  var reaplacementMap:Map[String,String] = Map[String,String]()
  
  /**
   * Take in a string and perform replacement.
   * 
   * @param		text									The string to replace words from
   * @param		{String}{binFile}			The model to use in sentence tokenization
   * @param		{Duration}{termTime]	The durationt to wait for replacement to finish.
   * @return	A List of List of words representing sentences void of punctuation
   */
  def replaceWords(text:String,binFile:String = "data/models/en-token.bin",termTime:Duration = Duration.Inf)={
    
    def replaceWords(sentence:String):Future[List[String]]=Future{
      var words = WordTokenizer.wordTokenize(PunctReplacer.replacePunct(text))
      words.foreach { x => ??? }
      null
    }
    
    SentTokenizer.load(binFile)
    Await.result(Future.traverse(SentTokenizer.getSentences(text).toList)(replaceWords(_)),termTime)
  }
  
}