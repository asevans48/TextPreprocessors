package com.simplrtek.disambiguation

import net.didion.jwnl.JWNL
import net.didion.jwnl.JWNLException
import net.didion.jwnl.data.{POS,PointerTarget,PointerType,Synset,Word,IndexWord}
import net.didion.jwnl.dictionary.Dictionary
import edu.cmu.lti.ws4j.impl.{WuPalmer,Resnik,Lesk}
import edu.cmu.lti.ws4j.RelatednessCalculator
import edu.cmu.lti.lexical_db.{ILexicalDatabase,NictWordNet}
import edu.cmu.lti.ws4j.util.WS4JConfiguration

/**
 * Subsumer based algorithms with a least common subsumer based algorithms.
 * This code can aid in the attainment by getting hypernyms and subsumers
 * or return scores based on ws4j
 */
class SubsumerBasedAlgorithms{
  
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
  
  def getResnik(worda : String, wordb : String)={
    val rc = new Resnik(db)
    rc.calcRelatednessOfWords(worda, wordb)
  }
  
  def getLeskForText(text : String)={
    val rc = new Lesk()
    rc.disambiguateSentence(text)
  }
}