package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector,DenseMatrix,SparseMatrix,SparseRowMatrix}
import com.simplrtek.enriched.RichDenseVector
import com.simplrtek.enriched.RichSparseVector
import com.simplrtek.enriched.EnrichedDenseMatrix

object Implicits {
  implicit def enrichDenseVector(vector:DenseVector)= new RichDenseVector(vector)
  implicit def enrichSparseVector(vector:RandomAccessSparseVector)= new RichSparseVector(vector)
  implicit def enrichDenseMatrix(matrix:DenseMatrix) = new EnrichedDenseMatrix(matrix)
  
}