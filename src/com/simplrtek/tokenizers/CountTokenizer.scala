package com.simplrtek.tokenizers

import com.simplrtek.preprocessors.{WordTokenizer,SentTokenizer}

/**
 * Take in Objects and CountVectorize with Scala Futures
 * 
 */
object CountTokenizer {
  
  /**
   * Vectorize a string and return a map containing the strings and counts. 
   * This method sent tokenizes, then word tokenizes before tokenizing. 
   * 
   * @param		text		The string to tokenize
   * @return	The list of maps containing string, count pairs
   */
  def  vectorizeString(text:String,binFile:String = "data/models/en-chunker.bin"):List[Map[String,Integer]]={
    var sents:List[Map[String,Integer]] = List[Map[String,Integer]]()
    for(sent <- SentTokenizer.getSentences(text, binFile)){
      var counts:Map[String,Integer]= Map[String,Integer]()
      for(word <- WordTokenizer.wordTokenize(sent)){
        val uw:String = word.toLowerCase().trim.replaceAll("[^a-z0-9#]+","")
        if(counts.contains(uw)){
          counts = counts.updated(uw, counts.get(uw).get + 1)
        }else{
          counts = counts + (uw -> 1)
        }
      }
      sents = sents :+ counts
    }
    
    sents
  }
  
  /**
   * Vectorize objects other than strings including words.
   * 
   * @param		tokenee		The list of objects to tokenize
   * @return 	The Map of object,count pairs
   * 
   */
  def vectorize[T](tokenee:List[T]):Map[T,Integer]={
    var counts:Map[T,Integer] = Map[T,Integer]()
    
    for(o <- tokenee){
      if(counts.contains(o)){
        counts = counts.updated(o, counts.get(o).get + 1)
      }else{
        counts = counts + (o -> 1)
      }
    }
    
    counts
  }
  
}