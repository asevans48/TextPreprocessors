package com.simplrtek.enriched.breeze

import breeze.linalg.DenseMatrix
import scala.concurrent.Future
class EnrichedDenseMatrix(matrix : DenseMatrix[Double]){
import scala.concurrent.ExecutionContext.Implicits.global

  
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
  
 def ** (e : Double):DenseMatrix[Double]={
   this.matrix.map { x => Math.pow(x, e)}
 }
 
 def linalgNorm():Future[Double]=Future{
   var v : Double = 0
   matrix.valuesIterator.foreach { x => v = v + x*x }
   Math.sqrt(v)
 }
 
}