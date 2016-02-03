package com.simplrtek.similarity

import net.didion.jwnl.JWNL
import net.didion.jwnl.JWNLException
import net.didion.jwnl.data.{POS,PointerTarget,PointerType,Synset,Word,IndexWord}
import net.didion.jwnl.dictionary.Dictionary
import edu.cmu.lti.ws4j.impl.{WuPalmer,Resnik,Lesk}
import edu.cmu.lti.ws4j.RelatednessCalculator
import edu.cmu.lti.lexical_db.{ILexicalDatabase,NictWordNet}
import edu.cmu.lti.ws4j.util.WS4JConfiguration
import com.simplrtek.preprocessors.PosTagger
import com.simplrtek.wordnet.WordnetAccess

import com.simplrtek.preprocessors.{WordTokenizer,PosTagger}
import com.simplrtek.preprocessors.TagConverter

import scala.concurrent.{Await,Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success,Failure}

/**
 * Subsumer based algorithms with a least common subsumer based algorithms.
 * This code can aid in the attainment by getting hypernyms and subsumers
 * or return scores based on ws4j
 */
class ICDisambiguation{
  private var wna : WordnetAccess = _
  private var tagger : PosTagger = _
  private val db : ILexicalDatabase  = new NictWordNet();
  WS4JConfiguration.getInstance().setMFS(true);
  
  /**
   * Recursively obtain all hypernyms for a synset
   */
  def getHypernyms(syns : Map[Synset,Int], inAllsyns: Map[Synset,Int], indepth : Int= 0): Map[Synset,Int]={
       var depth:Int = indepth
       var allsyns = inAllsyns
       if(allsyns.size >= 100){
         return allsyns
       }
       
       var hypernyms : Map[Synset,Int] = Map[Synset,Int]()
       
       
       syns.foreach { 
         s =>
          val hyp = s._1.getTargets(PointerType.HYPERNYM)
          hyp.foreach {
            target =>
              if(target.isInstanceOf[Synset]){
                hypernyms = hypernyms +(target.asInstanceOf[Synset] -> depth)
              }
          }
       }
       
       if(!hypernyms.isEmpty){
         if(allsyns.size + hypernyms.size >= 100){
           return allsyns
         }
         
         try{
           allsyns = allsyns ++ hypernyms
         }catch{
           case e:StackOverflowError => println(e.getMessage)
         }
         allsyns = getHypernyms(hypernyms,allsyns, depth + 1)
       }
       
       allsyns
  }
  
  /**
   * Calculate the least common subnomer between the sets.
   * This is a bit wierd because of wordnet and my own
   * need to research. It will need extensive testing
   * as it does not build a tree/graph to find the lowest
   * common node/subsumer/ancestor
   */
  def getLCS(syn1 : Synset, syn2 : Synset , pos : String ):(Synset,Int,Int,Int)={
    var s1:Map[Synset,Int] = Map[Synset,Int]()
    s1 = s1 + (syn1 -> 0)
    var h1:Map[Synset,Int] = Map[Synset,Int]()
    
    h1 = getHypernyms(s1,h1 )
    h1 = h1 +(syn1 -> 0)
    
    var s2 : Map[Synset,Int] =  Map[Synset,Int]()
    s2 = s2  + (syn2 -> 0)
    var h2 : Map[Synset,Int] = Map[Synset,Int]()
    h2 = getHypernyms(s2,h2)
    h2 = h2 +(syn2 -> 0)
    
    
    val intersection = h1.keySet.intersect(h2.keySet)
    
    if(intersection.size == 0){
      return null
    }
    
    //totaldistance
    var bestSet: Synset = null
    var bestDepth : Int = 0
    var deptha : Int = 0
    var depthb : Int = 0
    intersection.foreach { 
      x => 
        val d = h2.get(x).get + h1.get(x).get
        if(bestDepth < d){
          bestDepth = d
          bestSet = x
          deptha = h2.get(x).get
          depthb = h1.get(x).get
        }
    }
    
    
    (bestSet,bestDepth,deptha,depthb)
  }
  
  def calculateWuPalmer(worda : String, wordb : String):Double={
    val rc = new WuPalmer(db)
    rc.calcRelatednessOfWords(worda, wordb)
  }
  
  def getResnik(worda : String, wordb : String):Double={
    val rc = new Resnik(db)
    rc.calcRelatednessOfWords(worda, wordb)
  }
  
  def getLeskForWords(worda : String,wordb  : String):Double={
    val rc = new Lesk(db)
    rc.calcRelatednessOfWords(worda, wordb)
  }
  
  
  /**
   * Get a count of the overlapping sentences.
   * 
   * @param		The position of the word in the sentence
   * @param		The start position for the synsets of this word
   * @param		The end position for the synsets of this word
   * @param		The synset to use in comparison
   * @param		The synsets in totality
   * 
   * @return 	A future containing the array position, pointer index position, and the overlap
   */
  def getOverlaps(pos : Int, start : Int, end : Int,syn : Synset, synGlosses : List[List[String]]):Future[Int]=Future{
    var inter : Int = 0
    val gloss : List[String] = WordTokenizer.wordTokenize(syn.getGloss) ++ syn.getWords.map { x => x.getLemma }
    for(i <- 0 until synGlosses.size){
      if(i < start || i >= end){
         //get overlap of each 
          inter += gloss.intersect(synGlosses).size
      }
    }
    
    inter
  }
  
  /**
   * Disambiguate a sentence. Return a list of words and a list of their
   * appropriate sentences.
   * 
   * @param		The sentence
   * 
   * @return	 A list of words and a list of synsets as a tuple2	
   */
  def disambiguateSentence(sentence : String, batchSize : Int = 100, duration : Duration = Duration.Inf,minOverlaps : Int = 1):(List[String],List[String])={
    if(tagger == null){
      tagger= new PosTagger
    }
    
    if(wna == null){
      wna = new WordnetAccess
    }
    
    //get senses and sentences
    var ptrs : List[Int] = List[Int]()
    ptrs = ptrs :+ 0
    val sentences = sentence.split("\\!\\.,;").toList
    var stags : List[Synset] = List[Synset]()
    var overlaps : List[Integer] = List[Integer]()
    
    for(i <- 0 until sentences.size){
        val tags = tagger.tag(sentences(i)).split("[^A-Za-z0-9]+")
        
        for(j <- 0 until tags.size){
           val senses = tags.flatMap {  
            tagword =>
               val parr = tagword.split("_")
               wna.getSynset(TagConverter.getTag(parr(1)), parr(0)).asInstanceOf[List[Synset]]
          }.toList
          stags = stags ++ senses
          ptrs = ptrs :+ (ptrs(ptrs.size - 1 )+senses.size)
        }
    }
        
    
    //get overlaps
    var i : Int = 0
    var ptrIndex : Int = 0
    var start = 0
    var end = ptrs(i)
    
    var futs : List[Future[Int]] = List[Future[Int]]() 
    var stagGlosses : List[List[String]] = stags.map { x => WordTokenizer.wordTokenize(x.getGloss)}
    while(i < stags.size){
     //calculate overlaps
      futs = futs :+ this.getOverlaps(i, start, end,stags(i), stagGlosses)
      
      i+=1
      
      if(futs.size == 100 || i == stags.size){
        val r = Await.ready(Future.sequence(futs), duration).value.get
        r match {
          case Success(r) =>{
            overlaps ++ r
          }
          case Failure(r) => {
            println("Failed to Get Overlaps in Les for Sentence Disambiguation! \n"+r.getMessage)
            System.exit(-1)
          }
        }
        
      }
    }
    
    //here is where we can go lesk and just rip the max overlaps or use a cosines based approach
    //filtering out based on no or a number of overlaps required to form a decent connection
    
    //hash the vectors
    //https://github.com/ririw/scalaNMF
    
    //iterate ignoring non-fits and choosing in intervals
var senseChoices : List[Synset] = List[Synset]  ()
    start = 0
    end  = ptrs(0)
    
    
    
    null
  }
}