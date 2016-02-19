package com.simplrtek.math

import breeze.linalg.norm
import breeze.linalg.Transpose
import breeze.linalg.SparseVector
import breeze.linalg.{CSCMatrix,Matrix}

object CosineCalculator {
  
  
  def getCosines(documenta : SparseVector[Double],documentb : SparseVector[Double]):Double={
    documenta.dot(documentb) / (norm(documenta) * norm(documentb))
  }
  
  /**
   * Calculate Cosines of all documents in the matrix using a faily fast implementation.
   */
  def getCosines(documents : CSCMatrix[Double]):Matrix[Double]={
    println(documents)
    var vals = documents.t * documents
    val d = Math.pow(documents.valuesIterator.map { x => Math.pow(x, 2) }.sum, 2)
    for(i <- 0 until vals.rows){
      for(j <- 0 until vals.cols){
        vals(i,j) = vals(i,j)/d
      }
    }
    vals
  }
}