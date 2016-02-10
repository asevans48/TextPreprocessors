package com.simplrtek.math

import breeze.linalg.{CSCMatrix,Matrix}
import com.simplrtek.enriched.breeze.Implicits._

object CosineCalculator {
  
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