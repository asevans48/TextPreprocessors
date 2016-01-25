package com.simplrtek.enriched.breeze

import com.simplrtek.enriched.breeze.{EnrichedCSCMatrix,EnrichedDenseMatrix,EnrichedSparseVector,EnrichedVector}
import breeze.linalg.{DenseMatrix,Vector,CSCMatrix,SparseVector}

object Implicits {
  //Vectors
  implicit def enrichDenseVector(vector : Vector[Double])= new EnrichedVector(vector)
  implicit def enrichSparseVector(vector : SparseVector[Double])= new EnrichedSparseVector(vector)
  
  //Matrices
  implicit def enrichDenseMatrix(matrix : DenseMatrix[Double]) = new EnrichedDenseMatrix(matrix)
  implicit def enrichSparseMatrix(matrix : CSCMatrix[Double]) = new EnrichedCSCMatrix(matrix)
}