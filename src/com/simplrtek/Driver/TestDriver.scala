package com.simplrtek.Driver
import com.simplrtek.enriched.Implicits._
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}

import org.scalatest._
import org.scalatest.junit.AssertionsForJUnit
import scala.collection.mutable.ListBuffer
import org.junit.Assert._
import org.junit.Test
import org.junit.Before

import com.simplrtek.math.Smoother
import java.lang.ArrayIndexOutOfBoundsException

class SmoothingTest extends FlatSpec with Matchers{
  
  
  "an array of integers" should "return a triangularly smoothed result" in{
      //still need to add fractions to this test, was doing this manually because of laziness and my own spreadsheet based test docs
      //val arr=List(1,2,3,7,9,11,12,14,16,18)
      //Smoother.triangularSmoother(arr.asInstanceOf[List[Double]], 3) should equal (List(1,2,4.11,6.44,8.67,10.67,12.33,14.11,16,18))
     
  }
  
  "an m greater than array size" should "throw an array index out of bounds error" in{
    val arr=List(1.0,2.0,3.0,7.0,9.0,11.0,12.0,14.0,16.0,18.0)
    a [ArrayIndexOutOfBoundsException] should be thrownBy{
      Smoother.triangularSmoother(arr, m=100) 
    }
  }
  
}

class VectorTest extends FlatSpec with Matchers{
  
  "a sparse vector" should "be created from a dense array" in{
     var v:Vector = new DenseVector(Array(1.0,2.0,3.0))
     
  }
  
}

object TestDriver {
 
  def main(args:Array[String]):Unit={
    //val arr=List(1.0,2.0,3.0,7.0,9.0,11.0,12.0,14.0,16.0,18.0)
    //println(Smoother.simpleExponentialSmoother(arr, 3))
    var v:DenseVector = new DenseVector(Array(1.0,2.0))
    var v2:RandomAccessSparseVector = new RandomAccessSparseVector(6)
  }
  
}