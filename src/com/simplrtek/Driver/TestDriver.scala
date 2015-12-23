package com.simplrtek.Driver

import org.scalatest._
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import com.simplrtek.math.Smoother
import com.simplrtek.math.Smoother

class SmoothingTest extends FlatSpec with Matchers{
  
  
  "an array of integers" should "return a triangularly smoothed result" in{
      val arr=List(1,2,3,7,9,11,12,14,16,18)
      println(Smoother.triangularSmoother(arr.asInstanceOf[List[Double]], 3))
      Smoother.triangularSmoother(arr.asInstanceOf[List[Double]], 3) should equal (List(1,2,4.11,6.44,8.67,10.67,12.33,14.11,16,18))
  }
  /*
  "an m greater than array size" should "throw an array index out of bounds error" in{
  
  }
  
  "an array of negative integers" should "return a negative result" in{
    
  
  }
  
  "an integer array in descending order" should "return sorted results of the same as if it were ascending" in{
    
  }
  
  "an array of doubles" should "return a triangularly smoothed array of the same type" in{
    
  }
  
  "an array of longs" should "throw a number format error" in{
    
  }
  
  */
}

object TestDriver {
  
  def main(args:Array[String]):Unit={
    val arr=List(1.0,2.0,3.0,7.0,9.0,11.0,12.0,14.0,16.0,18.0)
    //val sm = new SmoothingTest
    //sm.execute()
    println(Smoother.triangularSmoother(arr.asInstanceOf[List[Double]], 3))
    
  }
  
}