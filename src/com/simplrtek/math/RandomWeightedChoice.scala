package com.simplrtek.math

object RandomWeightedChoice {
  
  def getIndex(distribution : List[Double],skip :scala.collection.mutable.ListBuffer[Integer]):Integer={
    var sum : Double = 0
    for(i <- 0 until distribution.length){
      if(skip == null || !skip.contains(i)){
        sum += distribution(i)
      }
    }
    sum = Math.random() * sum
    var currSum : Double = 0.0
    var index : Int = -1
    
    do{
      index += 1
      if(skip == null || !skip.contains(index)){
        currSum += distribution(index)   
      }
    }while(currSum < sum && index < distribution.length)
    
    if(index == distribution.size){
      index = (Math.random() * distribution.size).asInstanceOf[Integer]
    }
      
    index
  }
  
}