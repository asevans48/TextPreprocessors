package com.simplrtek.enriched.breeze

import breeze.linalg.CSCMatrix
import scala.concurrent.{Await,Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class EnrichedCSCMatrix(matrix : CSCMatrix[Double]) {

  
  def getNNZ():Int={
    matrix.activeSize
  }
  
  
  
  def getColNNZ():Vector[Integer]={
    val v = Vector.newBuilder[Integer]
    for(i <- 0 until matrix.cols){
      v += matrix(0 until matrix.rows, i to i).activeSize
    }
    v.result()
  }
  
  def getRowNNZ():Vector[Integer]={
    val v = Vector.newBuilder[Integer]
    for(i <- 0 until matrix.rows){
      v += matrix(i to i, 0 until matrix.cols).activeSize
    }
    v.result()
  }
  
 def ** (e : Double):CSCMatrix[Double]={
   this.matrix.map { x => Math.pow(x, e)}
 }
  
}