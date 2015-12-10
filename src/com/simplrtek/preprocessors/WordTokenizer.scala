package com.simplrtek.preprocessors

/**
 * A word tokenizer allowing for tokenziation using multiple regex patterns. 
 * Future implementations may include a trainable splitter that discovers 
 * words smashed together.
 */
object WordTokenizer {
  
  /**
   * Tokenize words with a regex using [^\s|\t|\r|\n|\r\n]+
   * 
   * @param		text		The string to tokenize
   * @return	The tokenized string
   */
  def wordTokenize(text:String):List[String]={
    """[^\s|\t|\r|\n|\r\n]+""".r.findAllIn(text).toList
  }
  
  /**
   * Parse words from text using \w+|\$[\d\.]+|\S+
   * 
   * @param		text		The text to split
   * @return	Tokenized text in a List[String]
   */
  def inclusiveWordTokenizer(text:String):List[String]={
    """\w+|\$[\d\.]+|\S+""".r.findAllIn(text).toList
  }
  
}