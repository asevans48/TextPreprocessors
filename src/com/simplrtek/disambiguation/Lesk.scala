package com.simplrtek.disambiguation

import com.simplrtek.preprocessors.TagConverter
import com.simplrtek.wordnet.WordnetAccess
import com.simplrtek.preprocessors.TagConverter
import com.simplrtek.preprocessors.SentTokenizer
import com.simplrtek.preprocessors.WordTokenizer
import edu.mit.jwi._
import edu.mit.jwi.item._
import com.simplrtek.preprocessors.Stemmer
import scala.collection.mutable.ArrayBuffer

import com.simplrtek.preprocessors.StopWords

import com.simplrtek.structures.Trie

import scala.collection.JavaConversions._

class Lesk {
  
  /**
   * Disambiguate a sentence returning a list of wordnet definitions that best fits the document.
   * 
   * @param			text		The text to disambiguage.
   * @retun 		A list of lists containing wordnet definitions.
   */
  def disambiguateSentence(text:String):Array[List[String]]={
     val wn = new WordnetAccess()
     var tries:List[Trie[Char]] = List[Trie[Char]]()
     
     var idx:Integer = 0
     var pointers:List[Integer] = List[Integer]()
     
     var senses = wn.getPOS(text).map { x => x.split("_").toList}
     senses.foreach { wtup => 
       wn.getSynset(TagConverter.getTag(wtup(1)), wtup(0)).foreach { 
         sense =>  
           val trie:Trie[Char] = new Trie[Char]
           WordTokenizer.wordTokenize(sense).foreach {
             x => 
               trie.contains(x.toCharArray(), true)
           }
           tries = tries :+ trie
           idx += 1
       } 
       pointers = pointers :+ idx
     }
   
     var overlaps:List[Integer] = List[Integer]()
     var curr = 0
     var i = 0
     while(curr < tries.size * tries.size){
       if(i % tries.size != curr){
         
       }
       curr += 1
     }
     
     null
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
  def disambiguate(word:String,senses:List[List[String]],arrpos:Integer):String={
     null
  }
  
}


object LeskDriver{
  
  
  def main(args:Array[String]):Unit={
    val lsk = new Lesk()
    lsk.disambiguateSentence("Fish live in the sea and fish are food.")
  }
}

object TestDriver{
  
  def main(args:Array[String]):Unit={
    val l = new  Lesk()
    l.disambiguateSentence("Fish live in water and people eat fish.")
  }
  
}