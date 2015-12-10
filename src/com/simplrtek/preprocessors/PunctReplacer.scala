package com.simplrtek.preprocessors

/**
 * Replaces punctuation but does not split to sentences.
 */
object PunctReplacer {
  
  /**
   * Replace Punctuation
   * 
   * @param		text		The string to remove punctuation from
   * @return	The text with punctuation removed.
   */
  def replacePunct(text:String):String={
    text.replaceAll("""\!|\.|\?|;|,|\-""","")
  }
  
}