package com.simplrtek.wordnet

import java.net.URL
import edu.mit.jwi._
import edu.mit.jwi.item._;
import java.io.File
import java.net.URL

class WordnetAccess(wndir:String = "/data/wordnet/" ){
  
  val dataDict = new Dictionary(new URL("file",null,wndir))
  dataDict.open()
  
  def getIdxWord()={
      
  }
  
  def getWordSense()={
    
  }
  
  def getGloss()={
    
  }
  
  def getLemma()={
    
  }
  
}