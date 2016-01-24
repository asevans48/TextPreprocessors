package com.simplrtek.preprocessors

import epic.models._
import epic.sequences.SemiCRF
import epic.trees.AnnotatedLabel
import java.io.File
import org.apache.commons.lang3.exception.ExceptionUtils
import opennlp.tools.sentdetect._

/**
 * A named entity extractor using Epic's Semi CRF Markov model.
 */
object NamedEntity {
  
  var ner:SemiCRF[AnnotatedLabel,String] = _
  var lang:SemiCRF[Any,String] = NerSelector.loadNer("en").get

  
  /**
   * Load the NER model.
   * 
   * @param	path	The java.io.File containing the model.
   */
  def load(path:File)={
    ner=deserialize[SemiCRF[AnnotatedLabel,String]](path.getAbsolutePath)
  }
  
  /**
   * Load the NER model from a string
   * 
   * @param	path		The String path to the model.
   */
  def load(path:String)={
    ner=deserialize[SemiCRF[AnnotatedLabel,String]](path) 
  }
  
  /**
   * 'Close' the NER model (send to GC)
   */
  def close()={
    ner = null
  }
  
  
  /**
   * Get the named entities with the entity and list of positions in the string.
   * 
   * @return	A Map containing named entities with a key of the entity and value of their list of positions in the provided string.
   */
  def getEntities(text:String):Map[String,List[Integer]]={
    var persons:List[String] = List[String]()
    var personIndices:Map[String,List[Integer]]= Map[String,List[Integer]]()
    try{
      for(sentence <- SentTokenizer.getSentencesFromML(text)){
        val txt=ner.bestSequence(sentence).render
        """\[[^\]:]+[\s:]+(.+?)?\]""".r.findAllMatchIn(txt).foreach { m => 
          persons = persons :+ m.group(1)  
        }
      }
      
      var startIndex:Integer=0
      for(person <- persons){
        startIndex=text.indexOf(person)
        var indices:List[Integer] = List[Integer]()
        while(startIndex > -1){
            indices = indices :+ startIndex
            startIndex = startIndex + 1
            startIndex = text.indexOf(person,startIndex)
        }
        personIndices = personIndices + (person -> indices)
      }
      
    }catch{
      case e:NullPointerException =>{
        println("Failed to Find Entities: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        sys.exit(-1)
      }
      case t:Throwable =>{
        println(t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        sys.exit(-1)
      }
    }
    personIndices
  }
  
  
  /**
   * Replace entities. Uses its own NER for speed.
   * 
   * @param	text	The string to replace entities in.
   * 
   * @return		A List[String] of sentences with named entities replaced.
   */
  def replaceEntities(text:String):List[String]={
    var strings:List[String] = List[String]()
    try{
      for(sentence <- SentTokenizer.getSentencesFromML(text)){
        val txt=ner.bestSequence(sentence).render
        strings = strings :+ txt.replaceAll("\\[[^\\]:]+:[^\\]]+\\]","PERSON")
      }
    }catch{
      case e:NullPointerException =>{
        println("Failed to Find Entities: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        sys.exit(-1)
      }
      case t:Throwable =>{
        println(t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        sys.exit(-1)
      }
    }
    println(strings)
    strings
  }
}