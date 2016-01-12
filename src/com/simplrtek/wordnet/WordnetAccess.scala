package com.simplrtek.wordnet

import java.net.URL
import java.io.File
import com.simplrtek.preprocessors.{PosTagger,SentTokenizer}
import java.net.URL
import net.didion.jwnl._
import net.didion.jwnl.dictionary.morph.DefaultMorphologicalProcessor
import net.didion.jwnl.dictionary.morph.DetachSuffixesOperation
import net.didion.jwnl.dictionary.Dictionary
import net.didion.jwnl.data.POS
import java.io.FileInputStream
import com.simplrtek.preprocessors.Stemmer
import scala.collection.JavaConversions._

/**
 * Offers general Wordnet Access. Functions include getting lemmas, getting synsets, getting POSTags.
 */
class WordnetAccess(properties:String){
   JWNL.initialize(new FileInputStream(properties))
   val dict:Dictionary = Dictionary.getInstance
  
   
   /**
    * Get POS tags.
    * 
    * @param		text		The String to split and tag.
    * @return		The array of array of taged sentences.
    */
   def getPOS(text:String):Array[Array[Array[String]]]={
     val tagger:PosTagger = new PosTagger()
     tagger.tagSentences(text)
   }
   
   /**
    * Get the lemmas for a string.
    * 
    * @param		text		The text to lemmatize.
    * @return	 	A list of strings.
    */
   def getLemma(text:String):List[String]={
     Stemmer.lemmatize(text).toList
   }
   
   /**
    * Get the Synset for the word.
    * 
    * @param		pos				The POS tag
    * @param		word			The word to get the synsets of
    * @return 	The list of glosses.
    */
   def getSynset(pos:POS, word:String):List[String]={
     val idw = dict.getIndexWord(pos, word)
     idw.getSenses().map({ x => x.getGloss }).toList
   }
}