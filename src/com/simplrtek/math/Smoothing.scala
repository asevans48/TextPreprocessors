package com.simplrtek.math

/**
 * A library of smoothers that can be used with text mining at teh moment. 
 * S-G filtering is not included yet as it is not needed. These smoothers 
 * try to eliminate all forms of noise to create a smooth graph while S-G will
 * preserve some peaks and valleys. 
 * 
 * Triangular Smoothing
 * Rectangular Smoothing
 * Hamming Smoothing
 * Hanning Smoothing
 * Simple Exponential Smoothing
 */
object Smoother {
  
  /**
   * Perform Triangular smoothing
   * 
   */
  def triangularSmoother(scores:List[Double],m:Integer = 5):List[Double]={
    
    if(scores.size < m){
      return scores
    }
    var d: List[Double] = scores.slice(0, m)
    var n = m
    while(n < scores.size - (m+1)){
      var score:Double = 0
      var denom:Integer=0
      var k = math.ceil(m/2).asInstanceOf[Integer]
      var j = (-k) +1
      while(j <= math.ceil(m/2).asInstanceOf[Integer]){
        score = score + ((k - math.abs(j)) * scores(n + j))
        denom = denom + (k - math.abs(j))
        j = j + 1
      }
      d = d :+ score/ (2*m)
      n = n + 1
    }
    d = d ++ d.slice(n,scores.size)
    d
  }
  
  /**
   * Perform rectangular smoothing which looks ahead as well as behind.
   * M can be specified but must always be odd.
   * 
   * @param		scores		A list of doubles to smooth
   * @return	A smoothed list of double
   */
  def rectangularSmoother(scores:List[Double],m:Integer = 3):List[Double]={
    if(scores.size < m){
      return scores
    }
    var d: List[Double] = scores.slice(0, m)
    var n = m
    while(n < scores.size - (m+1)){
      var score:Double = 0
       var k = math.ceil(m/2).asInstanceOf[Integer]
      var j = (-k) +1
      while(j < math.ceil(m/2).asInstanceOf[Integer]){
        score = score + scores(n + j)
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
    var d:List[Double] = scores.toList
    
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
  def hanningSmoother(scores:List[Double]):List[Double]={
    var d:List[Double] = List[Double]()
    for(i <- 0 to scores.size){
      d = d :+  ((.5 + .5 * Math.cos(2 * Math.PI * scores(i))) * scores(i))
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
    
    for(i <- 0 to scores.size){
      if(i > k){
        d = d :+ (d(i-1) + (scores(i) - scores(i-k))/k)  
      }else if(i>0){
        d = d :+ (d(0) + (scores(i)-scores(0)/i)) 
      }else{
        d = d :+ d(0)
      }
    }
      
    d
  }
  
  
}