package com.simplrtek.math

import breeze.linalg.SparseVector
import breeze.linalg.{CSCMatrix,Matrix}
import com.simplrtek.enriched.breeze.Implicits._

object CosineCalculator {
  
  
  def getCosines(documenta : SparseVector[Double],documentb : SparseVector[Double]):Double={
    documenta.dot(documentb) / (documenta.linalgNorm() * documentb.linalgNorm())
  }
  
  /**
   * Calculate Cosines of all documents in the matrix using a faily fast implementation.
   */
  def getCosines(documents : CSCMatrix[Double]):Matrix[Double]={
    var vals = (documents.t(0 to documents.rows, 0 to documents.cols) * documents)
    val d = Math.pow(documents.linalgNorm(), 2)
    for(i <- 0 until vals.rows){
      for(j <- 0 until vals.cols){
        vals(i,j) = vals(i,j)/d
      }
    }
    vals
  }
}