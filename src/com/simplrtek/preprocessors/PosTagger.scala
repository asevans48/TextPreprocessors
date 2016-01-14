package com.simplrtek.preprocessors

import edu.stanford.nlp.tagger.maxent.MaxentTagger
import java.io.File
import java.net.URL
import com.simplrtek.preprocessors.SentTokenizer
import net.didion.jwnl.data.POS

/**
 * The tag converter is necessary to work with JWI
 */
object TagConverter{
  
  /**
   * Convert a Penn Treebank tag to a Pos Tag
   * @param		posString		The tag to Convert
   * @return	A POS tag
   */
  def getTag(posString:String):POS={
   
      if(posString.startsWith("N") || posString.startsWith("n")){
        POS.NOUN
      }else if(posString.startsWith("V") || posString.startsWith("v")){
        POS.VERB
      }else if(posString.startsWith("J") || posString.startsWith("j")){
        POS.ADJECTIVE
      }else if(posString.startsWith("RB") || posString.startsWith("r")){
        POS.ADVERB
      }else{
        POS.ADVERB
      }
  }
  
  /**
   * Convert a post tag to a string.
   * 
   * @param			tag		The pos tag to use.
   * @return		The Penn Treebank string.
   */
  def getPenn(tag:POS):String={
    tag match{
      case POS.NOUN => "NN"
      case POS.VERB => "VB"
      case POS.ADVERB => "RB"
      case POS.ADJECTIVE => "JJ"
      case _ => null
    }
  }
  
}


/**
 * Several methods into the Stanford NLP Maxent Pos Tagger.
 * 
 */
class PosTagger(tagDir:String = "data/taggers/english-left3words-distsim.tagger"){
  val tagger:MaxentTagger = new MaxentTagger(tagDir)
  
  /**
   * Tag a string of text using the Stanfrod NLP tagger.
   * 
   * @param		text		The string to tag
   * @return	The String split on words with word,tag array.
   */
  def tag(text:String):Array[Array[String]]={
    tagger.tagString(text).split(" ").map { x => x.split("\\\\")}
  }
  
  /**
   * First find sentences and then tag the sentences.
   * 
   * @param		text		The string to split and tag.
   * @return	An array containing arrays of arrays of tags (word,tag).
   */
  def tagSentences(text:String):Array[Array[Array[String]]]={
    SentTokenizer.load()
    SentTokenizer.getSentences(text).map { x => tag(x) }
  }
}