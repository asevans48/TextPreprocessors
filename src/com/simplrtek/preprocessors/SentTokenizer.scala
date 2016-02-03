package com.simplrtek.preprocessors

import opennlp.tools.sentdetect._
import opennlp.tools.tokenize._
import epic.preprocess.{SentenceSegmenter,MLSentenceSegmenter,TreebankTokenizer}
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.{FileInputStream,IOException,File}
import sbt.io.IO
import com.simplrtek.logger.logger
import com.simplrtek.preprocessors.PosTagger

object SentTokenizer{
  var model:TokenizerModel = null 
  var tokenizer:Tokenizer = null
  var modelPath:String = null
  
  /**
   * Load the models. The default model is en-token.bin but this can be changed.
   * The default folder is data/models. This method must be called before tokenization.
   * The preceeding methods can be provided with a model, resulting in a call to this method. 
   * 
   * @param		{String}{binFile}		A binary file containing the model to use in training. (Pre-built models are recommended due to their normally enormous training sets)
   */
  def load(binFile:String = "data/models/en-token.bin")={
    if(tokenizer == null || modelPath == null || !modelPath.equals(binFile)){
      try{
        modelPath = binFile
        val modelIn = new FileInputStream(binFile)
        model =   new TokenizerModel(modelIn)
        tokenizer = new TokenizerME(model)
      }catch{
        case e:IOException => logger.log("error", "Failed to Tokenize Sentence: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e)) 
        case t:Throwable => logger.log("error","Failure in Sentence Tokenization: "+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
      }
    }
  }
  
  /**
   * Get Sentences from a file.
   * 
   * @param		file								The file to obtain sentences from.
   * @param		{String}{binFile}		The string path to the binFile.
   * @see	SentTokenizer#getSentences(String sentences)
   * @see	SentTokenizer#load(String binFile)
   */
  def getSentencesFromFile(file:File,binFile:String = null):Array[String]={
    if(binFile != null && (modelPath == null || !modelPath.equals(binFile))){
      load(binFile)
      modelPath = binFile
    }
    return getSentences(IO.read(file))
  }
  
  /**
   * Get Sentences From a String
   * 
   * @param			sentences		The string to parse
   * @return		The array of tokenized strings
   * @see SentTokenizer#load(String binFile)
   */
  def getSentences(sentences:String,binFile:String = "data/models/en-token.bin"):Array[String]={
    try{
      if(binFile != null && (modelPath == null || !modelPath.equals(binFile))){
        load(binFile)
        modelPath = binFile
      }
      
      tokenizer.tokenize(sentences)
    }catch{
      case e:NullPointerException =>{
        logger.log("error","Tokenizer Error: Please Check That Tokenizer and Model are instantiate\n"+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        sys.exit(-1)
      }
      case t:Throwable =>{ 
        logger.log("error","Error In Getting Sentences: "+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        sys.exit(-1)
      }
    }
  }
  
  /**
   * Use a regular expression to split sentences. This may
   * work better for certain data. 
   * 
   * @param		sents		The text to split.
   * @return	A list of split sentences.
   */
  def getSentencesFromRegex(sents : String) : List[String] ={
    """(?mis)[^.?!]+""".r.findAllMatchIn(sents).toList.map { x => x.group(0) }
  }
  
  
  /**
   * This method gets sentences from the EPIC API as opposed to openNLP.
   * This method uses the TreeBank Tokenizer.
   * 
   * @param		sentences		The string to get sentences from.
   * @return	And IndexedSeq[IndexedSeq[String]] of sentences.
   */
  def getSentencesFromML(sents:String): IndexedSeq[IndexedSeq[String]]={
    val text:IndexedSeq[IndexedSeq[String]] = null
    val sentenceSplitter = MLSentenceSegmenter.bundled().get
    val tokenizer = new TreebankTokenizer()
     sentenceSplitter.apply(sents).map(tokenizer).toIndexedSeq
  }
}