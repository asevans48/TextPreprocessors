package com.simplrtek.preprocessors

import edu.stanford.nlp.pipeline._
import edu.stanford.nlp.ling.CoreAnnotations._

import java.util.Properties
import scala.collection.JavaConversions._

import scala.collection.mutable.ArrayBuffer

/**
 * A single threaded Stemmer for small batch processing.
 */
object Stemmer {
  
  /**
   * Performs the lemmatization.
   * 
   * @param		text				The string to lemmatize
   * @param		stopWords		A set[String] containing the stopwords.
   * @param		pipeline		The Stanford Core NLP pipeline with the last piece being 'lemma'.
   * @return	The sequence of lemmatized strings Seq[String]].
   */
  def plaintTextToLemmas(text:String,stopWords:Set[String],pipeline:StanfordCoreNLP):Seq[String]={
    val doc = new Annotation(text)
    pipeline.annotate(doc)
    val lemmas = new ArrayBuffer[String]()
    val sentences = doc.get(classOf[SentencesAnnotation])
    for (sentence <- sentences; token <- sentence.get(classOf[TokensAnnotation])) {
      val lemma = token.get(classOf[LemmaAnnotation])
      if (lemma.length > 2 && !stopWords.contains(lemma)) {
        lemmas += lemma.toLowerCase
      }
    }
    lemmas
  }
  
  /**
   * Lemmatize a single text.
   * 
   * @param			text		The string to lemmatize
   * @return		Returns a Seq[String] containing the lemmatized strings.
   * @see Stemmer#plainTextToLemma
   */
  def lemmatize(text:String):Seq[String]={
    val props = new Properties()
    props.put("annotators", "tokenize, ssplit, pos, lemma")
    val pipeline = new StanfordCoreNLP(props)
    plaintTextToLemmas(text,StopWords.stopList.toSet, pipeline)
  }
}