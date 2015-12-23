package com.simplrtek.math

import reflect._

/**
 * A library of smoothers that can be used with text mining at teh moment. 
 * S-G filtering is not included yet as it is not needed. These smoothers 
 * try to eliminate all forms of noise to create a smooth graph while S-G will
 * preserve some peaks and valleys. 
 * 
 * These classes return lists of doubles to match the data returned by breeze and mahout.
 * 
 * Triangular Smoothing
 * Rectangular Smoothing
 * Hamming Smoothing
 * Hanning Smoothing
 * Savitsky-Gavoy
 * Simple Exponential Smoothing
 */
object Smoother {

  /**
   * Test for things that fit into Double.
   * @param	Any ref
   */
  def testDoubles(scores:Any)={
    scores match{
      case x:Long =>{
        try{
          throw new NumberFormatException("FOUND LONG: Data must be of type Integer, short, or Double")
        }catch{
          case t:NumberFormatException =>{
            t.getMessage
            sys.exit(1001001)
          }
        }
        
      }
      case x:Float =>{
        try{
          throw new NumberFormatException("FOUND Float: Data must be of type Integer, short, or Double")
        }catch{
          case t:NumberFormatException =>{
            t.getMessage
            sys.exit(1001001)
          }
        }
        
      }
      case _ =>{
        println("Proper Type")
      }
    }
  }
  
  /**
   * Perform Triangular smoothing on a List containin anything that works
   * with a double (throws numberformat exception otherwise).
   *
   * @param		scores		The scores to smooth
   * @param		m					The smoothing score to use
   * @return	A list of smoothed scores.
   */
  def triangularSmoother(inScores:List[Double],m:Integer = 5):List[Double]={
    
    var scores = inScores.sorted

    if(m > scores.size){
      println("Array must be at least m")
      return scores
    }
    
    var smoothScores:List[Double] = List[Double]()
    var d: List[Double] = scores.slice(0, m).asInstanceOf[List[Double]]
    var n = m - 1
    while(n <= scores.size - m){
      var k = -1 * (m -1)
      var score: Double = 0
      var denom: Double = 0
      while(k < m){
        val x = m-math.abs(k)
        denom += x
        score += (x * scores(n + k))
        k += 1
      }
      n += 1
      smoothScores = smoothScores :+ (score/denom)
    }
    
    smoothScores = scores.slice(0, m-1) ++ smoothScores
    smoothScores ++ scores.slice(scores.size - (m-1), scores.size)
  }
  
  /**
   * Perform rectangular smoothing which looks ahead as well as behind.
   * M can be specified but must always be odd.
   * 
   * @param		scores		A list of doubles to smooth
   * @return	A smoothed list of double
   */
  def rectangularSmoother(scores:List[Double],m:Integer = 3):List[Double]={
   
    var d: List[Double] = scores.slice(0, m).asInstanceOf[List[Double]]
    var n = m
    while(n < scores.size - (m+1)){
      var score:Double = 0
       var k = math.ceil(m/2).asInstanceOf[Integer]
      var j = (-k) +1
      while(j < math.ceil(m/2).asInstanceOf[Integer]){
        score = score + scores(n + j).asInstanceOf[Double]
        j = j + 1
      }
      d = d :+ (score/ m)
      n = n + 1
    }
    d = d ++ d.slice(n,scores.size)
    d
  }
  
  /**
   * Performs Hamming Smoothing.
   * 
   * @param		scores		The list of doubles to smooth
   * @return	A list of smoothed doubles.
   */
  def hammingSmoother(scores:List[Double]):List[Double]={
    var m = scores.size/2
    var r = Math.PI  / m
    var d:List[Double] = scores.toList.asInstanceOf[List[Double]]
    
    var n = -m
    while(n < m){
      d = d.updated(m + n,(.54 + .46 * Math.cos(n*r)) * d(m+n))
      n = n + 1
    }
    
    d
  }
  
  /**
   * Performs Smoothing with a Hanning window to bring about better windows.
   * 
   * @param		scores		The scores to smooth as a list of doubles
   * @return	The list of smoothed doubles.
   */
  def hanningSmoother[T](scores:List[T]):List[Double]={
    var d:List[Double] = List[Double]()
    
    if(scores.size > 0){
      testDoubles(scores)
      
      for(i <- 0 to scores.size){
        d = d :+  ((.5 + .5 * Math.cos(2 * Math.PI * scores(i).asInstanceOf[Double])) * scores(i).asInstanceOf[Double])
      }
    }
    d
  }
  
  /**
   * Perform basic smoothing  simple exponential smoothing with a certain number of prior variables
   * taken into consideration. Uses a moving average.
   * 
   * @param		scores		A list of doubles to smooth
   * @param		k					The number of prior variables to take into account.
   * @return	A list of doubles consisting of the smoothed variables		
   */
  def simpleExponentialSmoother(scores:List[Double],k:Integer = 3):List[Double]={
    
    var d:List[Double] = List[Double]()
    
    if(scores.size > 0){
      testDoubles(scores)
      
      for(i <- 0 to scores.size){
        if(i > k){
          d = d :+ (d(i-1) + (scores(i).asInstanceOf[Double] - scores(i-k).asInstanceOf[Double])/k)  
        }else if(i>0){
          d = d :+ (d(0) + (scores(i).asInstanceOf[Double]-scores(0).asInstanceOf[Double]/i)) 
        }else{
          d = d :+ d(0)
        }
      }
    }
    d
  }
  
  
}