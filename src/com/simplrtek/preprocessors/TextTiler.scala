package com.simplrtek.preprocessors

import org.slf4j.{Logger,LoggerFactory}
import scala.collection.mutable.ListBuffer
import com.simplrtek.math.Smoother
import breeze.linalg.{Vector,DenseVector}
import breeze.stats.{variance,mean,stddev}

case class TokenSequence(var index:Int,var indexList:List[(String,Int)],var originalLength:Int)
case class TokenTableField(var firstPos:Int,var tsOccurrences:List[List[Int]],var totalCount:Int = 1,var parCount:Int = 1,var lastPar:Int = 0,var lastTokSeq:Int = -1)

/**
 * Segments Text into its components and saves them to a directory. The implementation is similar to that in  @see <a href="http://www.nltk.org/_modules/nltk/tokenize/texttiling.html"> Python NLTK </a>
 * This algorithm should be distributable via spark and map partitions. Differences exist. See the resources for what was used to check the algo. and the paper it is based on.
 * 
 * The smoothing scores are a bit different than nltk but the main flat one should be the same simple exponential smoothing. NLTK is the most generic implementation I have found.
 * 
 * @see <a href="http://www.nltk.org/_modules/nltk/tokenize/texttiling.html">Python NLTK</a>
 * @see <a href="http://arxiv.org/pdf/1503.05543.pdf"> Text Segmentation Based on Semantic Word Embeddings</a>
 * @see <a href="http://www.jlcl.org/2012_Heft1/jlcl2012-1-3.pdf">Text Segmentation with Topic Models</a>
 * @see <a href="http://www.cs.toronto.edu/~kazemian/textsegsum.pdf">Text Segmentation and Summarization</a>
 * 
 * @see DistributedTextTiler#stem
 */
class TextTiler(w:Integer = 20, k:Integer = 10,stopwords:Set[String] = StopWords.stopList.toSet,smoothingWidth:Integer = 2, smoothing_rounds:Integer = 1){
  var log:Logger = LoggerFactory.getLogger(classOf[TextTiler])
  var tokenTable:Map[String,Integer] = Map[String,Integer]()
  
  
  /**
   * Mark Paragraphs
   * 
   * @param		text		The text to mark.
   * @return	A List[Int] of paragraph breaks with position 0 as a gaurunteed break.
   */
  def markParagraphBreaks(text:String):List[Int]={
    var last_break:Integer = 0
    var pbreaks:List[Int] = List(0)
    val MIN_PARAGRAPH:Integer = 100
    """[ \t\r\f\v]*\n[ \t\r\f\v]*\n[ \t\r\f\v]*""".r.findAllMatchIn(text).foreach { 
      mtch =>
         if((mtch.start - last_break) >= MIN_PARAGRAPH){
           pbreaks = pbreaks :+ mtch.start
           last_break = mtch.start
         }
        
    }
    pbreaks
  }

  
  /**
   * Divide text into token sequences.
   * 
   * @param		text		The text to split into sequences.
   */
  def divideToTokenSquences(text:String):List[TokenSequence]={
     var indexList:List[(String,Int)] = null
     """\w+""".r.findAllMatchIn(text).foreach { mtch => indexList = indexList :+ (mtch.group(0),mtch.start)}
     var i:Int =0
     var tokList:List[TokenSequence] = List[TokenSequence]()
     while(i < indexList.length){
       tokList = tokList :+ TokenSequence((i/w).asInstanceOf[Int],indexList.slice(i,i+w),indexList.size)
     }
     tokList
  }
  
  /**
   * Create The token Table which creates the bag of words implementation.
   * 
   * @param		tokSeqs			The List of token sequences
   * @param		parBreaks		List of paragraph breaks
   * 
   * @return	A map of word to the token table field containing counts and indices.
   */
  private def createTokenTable(tokSeqs:List[TokenSequence],parBreaks:List[Int]):Map[String,TokenTableField]={
    var tokenTable:Map[String,TokenTableField] = Map[String,TokenTableField]()
    var currentPar:Int = 0
    var currentTokSeq:Int = 0
    val pbIter = parBreaks.iterator
    var currentParBreak = pbIter.next
    
    if(currentParBreak == 0){
      try{
        currentParBreak = pbIter.next()
      }catch{
        case t:Throwable => log.error("No Paragraph Breaks Were Found. \n"+t.getMessage+"\n"+t.getStackTraceString)
      }
    }
    
    for( sequence <- tokSeqs){
      for(tup <- sequence.indexList){
        try{
          while(tup._2 > currentParBreak && pbIter.hasNext){
            currentParBreak = pbIter.next()
            currentPar += 1
          }
        }catch{
          case t:Throwable => log.error("Iteration Error: %s".format(t.getMessage))
        }
        
        if(tokenTable.contains(tup._1)){
          tokenTable.get(tup._1).get.totalCount = tokenTable.get(tup._1).get.totalCount + 1
          
          if(tokenTable.get(tup._1).get.lastPar  != currentPar){
            tokenTable.get(tup._1).get.lastPar = currentPar
            tokenTable.get(tup._1).get.parCount = tokenTable.get(tup._1).get.parCount + 1 
          }
          
          if(tokenTable.get(tup._1).get.lastTokSeq != currentTokSeq){
            tokenTable.get(tup._1).get.lastTokSeq = currentTokSeq
            tokenTable.get(tup._1).get.tsOccurrences = tokenTable.get(tup._1).get.tsOccurrences :+ List(currentTokSeq,1)
          }else{
            tokenTable.get(tup._1).get.tsOccurrences.updated(-1, tokenTable.get(tup._1).get.tsOccurrences(-1).updated(1,tokenTable.get(tup._1).get.tsOccurrences(-1)(1) + 1)) 
          }
        }else{
          tokenTable = tokenTable + (tup._1 -> TokenTableField(firstPos = tup._2, tsOccurrences = List(List(currentTokSeq,1)),totalCount = 1,parCount = 1,lastPar = currentPar, lastTokSeq = currentTokSeq))
        }
      }
    }
    tokenTable
  }
  
  
  /**
   * Perform TFIDF using vectorization on a bag of words.
   * 
   * @param		tokSeqs			The token sequences fields.
   * @param		tokTable		The token table. 
   * 
   * @return 	A double vector.
   */
  def blockComparison(tokSeqs:List[TokenSequence],tokTable:Map[String,TokenTableField]):List[Double]={
    
    def blk_frq(tok:(String,TokenTableField),block:List[Int]):Double={
      var tsOccs = tokTable.get(tok._1).get.tsOccurrences.filter { x =>  block.contains(x) }
      var freq: Double = 0.0
      tsOccs.foreach { x => freq += x(1)}
      freq
    }
    
    var gapScores:List[Double] = List[Double]()
    var numGaps:Integer = tokSeqs.size - 1
    var windowSize = 0
    
    for(currGap <- 0 to numGaps){
      var scoreDividend:Double = 0
      var scoreDivisorB1:Double = 0 
      var scoreDivisorB2:Double = 0
      var score:Double = 0
      
      if(currGap < this.k -1){
        windowSize = currGap + 1
      }else if(currGap > numGaps - this.k){
        windowSize = numGaps - currGap
      }else{
        windowSize = this.k
      }
      
      var b1 = tokSeqs.slice((currGap - windowSize) + 1, currGap + 1).map{ ts => ts.index}
      var b2 = tokSeqs.slice(currGap + 1, currGap + windowSize + 1).map { ts => ts.index }
      
      for( t <- tokTable){
        scoreDividend += blk_frq(t,b1) * blk_frq(t,b2)
        scoreDivisorB1 += Math.pow(blk_frq(t,b1),2)
        scoreDivisorB2 += Math.pow(blk_frq(t,b2),2)
      }
      
      try{
        score = scoreDividend / math.sqrt(scoreDivisorB1*scoreDivisorB2)
      }catch{
        case t:Throwable => t.getMessage+"\n"+t.getStackTraceString
      }
      
      gapScores = gapScores :+ score
    }
    
    gapScores
  }
  
  def smooth(scores:List[Double],windowLen:Integer,stype:String = "simple"):List[Double]={
    var s:List[Double] = List[Double]()
    if(scores.length < windowLen){
      try{
        throw new NumberFormatException("Number of scores must be less than window length")
      }catch{
        case t:Throwable => t.getMessage +"\n"+t.getStackTraceString
      }
     
     
     if(stype.equals("simple")){
       s=Smoother.simpleExponentialSmoother(scores)
     }else if(stype.equals("hanning")){
       s=Smoother.hanningSmoother(scores)
     }else if(stype.equals("hamming")){
       s=Smoother.hammingSmoother(scores)
     }else{
     
       try{
         throw new NullPointerException
       }catch{
         case t:Throwable => log.error("Filter Not Found ("+stype+")"+t.getStackTraceString)
       }
     }
     
      
    }
    s
  }
  
  /**
   * Depth scores from chosen points as done in NLTK
   * 
   * @param		scores		The scores to use in finding the depthScores
   * @return	The list of doubles representing the depths
   */
  def depthScores(scores:List[Double]):List[Double]={
    var depthScores:List[Double] = scores.map{ x => 0.0 }
    var clip = math.min(math.max(scores.size / 10,2),5)
    var index = clip
    
    for(gapscore <- scores.slice(clip, -clip)){
      var lpeak = gapscore
      var j = 0
      var sl = scores.slice(index, -1)
      var run:Boolean = true
      while( j < sl.size && run){
        if(sl(j) >= lpeak){
          lpeak = sl(j)
        }else{
          run = false
        }
        j += 1
      }
      
      var rpeak = gapscore
      j=0
      sl = scores.slice(index,scores.size)
      run = true
      while(j < sl.size && run){
        if(sl(j) >= rpeak){
          rpeak = sl(j)
        }else{
          run = false
        }
        j += 1
      }
      
      
      depthScores.updated(index, lpeak + rpeak - (2 * gapscore))
      index += 1
    }
    
    depthScores
  }
  
  
  /**
   * Depth scores based on a statistical approach (a second common approach)
   * 
   * @param		scores		The list of doubles to use in getting the depth scores
   * @param		c					The number of standard deviations to consider for breakpoints (usually 0.5 or 1.0)
   * @return	The depth scores as a list of doubles
   */
  def depthScoresFromStats(scores:List[Double],c:Double = 0.5):List[Double]={
    
    //create vector
    var v:DenseVector[Double] = new DenseVector(scores.toArray)
    var score:Double =  mean(v) - (stddev(v) * c)
    
    //find points above the new threshold to set
    v.toScalaVector().toList.filter { x => x > score }
  }
  
  
  /**
   *Smooth a list of doubles with default smoothing
   * 
   * @param		gapScores		The list of doubles to smooth
   * @return	A list of smoothed doubles
   */
  def smoothScores(gapScores:List[Double]):List[Double]={
    smooth(gapScores,this.smoothingWidth + 1)
  }
  
  /**
   * Normalize boundaries to the nearest break.
   * 
   * @param		text						The string to use
   * @param		boundaries			The discovered boundaries
   * @param		parBreaks				The list of paragraph breaks
   * @return 	A list of doubles including the normalized boundaries
   */
  def normalizeBoundaries(text:String,boundaries:List[Double],parBreaks:List[Int]):List[Double]={
    var normBoundaries:List[Double] = List[Double]()
    var charCount:Integer = 0
    var wordCount:Integer = 0
    var gapsSeen:Integer = 0
    var seenWord:Boolean = false
    
    for(chr <- text){
      charCount += 1
      if(chr == ' ' || chr == '\t' || chr == '\n' && seenWord){
        seenWord = false
        wordCount += 1
      }else{
        seenWord = true
      }
      
      if(gapsSeen < boundaries.size && wordCount > math.max(gapsSeen*this.w,this.w)){
        if(boundaries(gapsSeen) == 1){
          var bestFit:Double = text.length
          var bestBr:Double = 0
          var run:Boolean = true
          var i:Integer = 0
          
          while(i < parBreaks.size && run){
            var br =parBreaks(i)
            if(bestFit > math.abs(br - charCount)){
              bestFit = math.abs(br - charCount)
              bestBr = br
            }else{
              run = false
            }
            i += 1
          }
          
          if(normBoundaries.contains(bestBr)){
            normBoundaries = normBoundaries :+ bestBr
          }
        }
        gapsSeen += 1
      }
      
    }
   normBoundaries
  }
  
  /**
   * Identify boundaries using statistics
   * 
   * @param		scores		The list of scores to identify boundaries with.
   * @return	The boundaries.
   */
  def identifyBoundaries(scores:List[Double]):List[Double]={
    var boundaries = new DenseVector(scores.toArray)
    var m = mean(boundaries)
    var std = stddev(boundaries)
    
    var cutoff = m - 2.0 * std
    var depthTuples = scores.zip((0 to scores.size)).sorted
    depthTuples = depthTuples.reverse
    var hp = depthTuples.filter( x => x._1 > cutoff)
    
    var barr = boundaries.toScalaVector().toList
    
    for(dt <- hp){
      barr.updated(dt._1.asInstanceOf[Int], 1)
      for(dt2 <- hp){
        if(dt._1 != dt2._1 && math.abs(dt2._1 - dt._1) < 4 && barr(dt2._1.asInstanceOf[Int]) == 1){
          barr.updated(dt._1.asInstanceOf[Int], 0)
        }
      }
    }
    barr
  }
  
  /**
   * Perform Tokenization.
   * 
   * @param		text		The string to segment.
   * @return	A List[String] of segments.
   */
  def segment(text:String):List[String]={
    var utext=text.toLowerCase
    StopWords.stopList.foreach { 
      x => utext = utext.replaceAll(x.toLowerCase,"stopword") 
    }
    
    utext=Stemmer.lemmatize(utext).toList.mkString("\n")
    utext = utext.replaceAll("[!\\?\\.,;:'\"\\-]+","")
    var tokSeqs:List[String] = List[String]()
    val ws = Math.floor(utext.split("\\s+").length/w).asInstanceOf[Integer]
    val parBreaks= this.markParagraphBreaks(utext)
    val tokseqs = this.divideToTokenSquences(utext)
    tokseqs.foreach { x => x.indexList = x.indexList.filter(w => !StopWords.stopList.contains(w))}
    var tokTable = this.createTokenTable(tokseqs,parBreaks)
    
    val smooth_scores = smoothScores(blockComparison(tokseqs,tokTable))
    val depth_scores = depthScores(smooth_scores)
    val normBoundaries = normalizeBoundaries(text,identifyBoundaries(depth_scores),parBreaks)
    
    var segmentedText:List[String] = List[String]()
    var prevb:Integer = 0
    
    for(b <- normBoundaries){
      
      if(b != 0){
        segmentedText = segmentedText :+ text.slice(prevb, b.asInstanceOf[Int])
        prevb = b.asInstanceOf[Int]
      }
    }
    
    if(prevb < text.length){
       segmentedText = segmentedText :+ text.slice(prevb, text.length)
    }
    
    if(segmentedText.size ==0){
      segmentedText = List(text)
    }
    
       
    segmentedText
  }
  
}