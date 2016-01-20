package com.simplrtek.disambiguation

import com.simplrtek.preprocessors.TagConverter
import com.simplrtek.wordnet.WordnetAccess
import com.simplrtek.preprocessors.TagConverter
import com.simplrtek.preprocessors.SentTokenizer
import com.simplrtek.preprocessors.WordTokenizer
import net.didion.jwnl.data.Synset
import net.didion.jwnl.data.POS
import com.simplrtek.preprocessors.Stemmer
import scala.collection.mutable.ArrayBuffer

import com.simplrtek.preprocessors.StopWords

import com.simplrtek.structures.Trie

import scala.collection.JavaConversions._

class Lesk {
  SentTokenizer.load()
  
  /**
   * Disambiguate a sentence returning a list of wordnet definitions that best fits the document.
   * 
   * @param			text		The text to disambiguage.
   * @retun 		A list of lists containing wordnet definitions.
   */
  def disambiguateText(text:String):List[List[(String,POS,String)]]={
     SentTokenizer.getSentences(text).map { s => disambiguateSentence(s) }.toList
  }
  
  /**
   * Disambiguate a sentence.
   * 
   * @param 		text		The sentence as text
   * 
   * @return		A list of senses
   */
  def disambiguateSentence(text:String):List[(String,POS,String)] ={
     //split to words
     val wn = new WordnetAccess()
     val posArr = wn.getPOS(text)
     val words = WordTokenizer.wordTokenize(text).filter { x => !StopWords.stopList.contains(x.toLowerCase) }
     
     //get senses
     var senseArr: List[List[Synset]] = List[List[Synset]]()
     for(i <- 0 until posArr.size){
        if(posArr(i) != null){
          val pos = TagConverter.getTag(posArr(i).replaceAll(".*?_",""))
          if(pos != null){
            senseArr = senseArr :+ wn.getSenses(pos, words(i)).asInstanceOf[List[Synset]]
          }else{
            senseArr = senseArr :+ null
          }
        }
     }
     
     val senseStrings = senseArr.map{ x => 
       if(x != null){
         x.map{ y =>
           if(y != null) y.getGloss else " "  
         }.mkString
       }else{
         " "
       }
     }
     
     var retArr:List[(String,POS,String)] = List[(String,POS,String)]()
     for(i <- 0 until words.size){
       retArr = retArr :+ disambiguate(words(i),TagConverter.getTag(posArr(i).replaceAll(".*?_","")),senseArr(i),i,senseStrings)
     }

     retArr
  }
  
  /**
   * Performs the lesk based algorithm and then return the definition that
   * is most appropriate for the word in the sentence.
   * 
   * @param			word					The word to disambiguate
   * @param			senses				The senses of the word
   * @param			posList				The POS tags for the sentence
   * @param			words					A list of words
   * @param			senseStrings	The sense glosses combined.
   * @return		The definition that best fits the string.
   */
  def disambiguate(word:String,pos:POS,senses:List[Synset],sensePos: Integer,senseStrings : List[String]):(String,POS,String)={
     println(word)
     println(pos)
     println(senses)
     println(senseStrings)
     println(" ")
     if(pos == null || senses == null){
       return (word,pos,null)
     }
     
     
     
     null
  }
  
}


object LeskDriver{
  
  
  def main(args:Array[String]):Unit={
    val lsk = new Lesk()
    lsk.disambiguateSentence("Fish live in the sea and fish are food.")
  }
}