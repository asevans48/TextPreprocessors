package com.simplrtek.disambiguation


import com.simplrtek.preprocessors.SentTokenizer
import com.simplrtek.preprocessors.WordTokenizer
import edu.mit.jwi._
import edu.mit.jwi.item._
import com.simplrtek.preprocessors.Stemmer
import scala.collection.mutable.ArrayBuffer

import com.simplrtek.preprocessors.StopWords

import scala.collection.JavaConversions._

class Lesk {
  
  /**
   * Disambiguate a sentence returning a list of wordnet definitions that best fits the document.
   * 
   * @param			text		The text to disambiguage.
   * @retun 		A list of lists containing wordnet definitions.
   */
  def disambiguateSentence(text:String):Array[List[String]]={
    var disArr:ArrayBuffer[List[String]] = new ArrayBuffer()
   
    val tokens = WordTokenizer.wordTokenize(text).filter { x => !StopWords.stopList.contains(x) }
    for( i <- 0 until tokens.size){
       disArr = disArr :+ disambiguate(tokens(i),text,i)
    }

    disArr.toArray
  }
  
  /**
   * Performs the lesk based algorithm and then return the definition that
   * is most appropriate for the word in the sentence.
   * 
   * @param			word					The word to disambiguate.
   * @param			text					The text to use in the disambiguation.
   * @param			pos						The word position in the string.
   * @return		The definition that best fits the string.
   */
  def disambiguate(word:String,text:String,pos:Integer):List[String]={
      val lemmas = Stemmer.lemmatize(text)
      println("Lemmas")
      println(lemmas)
      val wordLem = lemmas(pos)
      for(lemma <- lemmas){
         //println(lemma)
      }
      
      null
  }
  
}

object TestDriver{
  
  def main(args:Array[String]):Unit={
    val l = new  Lesk()
    l.disambiguateSentence("Fish live in water and people eat fish.")
  }
  
}