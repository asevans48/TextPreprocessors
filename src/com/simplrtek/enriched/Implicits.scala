package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector,DenseMatrix,SparseMatrix,SparseRowMatrix}
import com.simplrtek.enriched.RichDenseVector
import com.simplrtek.enriched.RichSparseVector
import com.simplrtek.enriched.EnrichedDenseMatrix
import com.simplrtek.enriched.EnrichedDenseMatrix
import com.simplrtek.enriched.EnrichedSparseRowMatrix
import com.simplrtek.enriched.EnrichedSparseMatrix

object Implicits {
  //Vectors
  implicit def enrichDenseVector(vector:DenseVector)= new RichDenseVector(vector)
  implicit def enrichSparseVector(vector:RandomAccessSparseVector)= new RichSparseVector(vector)
  
  //Matrices
  implicit def enrichDenseMatrix(matrix:DenseMatrix) = new EnrichedDenseMatrix(matrix)
  implicit def enrichSparseMatrix(matrix:SparseMatrix) = new EnrichedSparseMatrix(matrix)
  implicit def enrichSparseRowMatrix(matrix:SparseRowMatrix) = new EnrichedSparseRowMatrix(matrix)
}