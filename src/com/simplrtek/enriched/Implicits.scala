package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.enriched.RichDenseVector
import com.simplrtek.enriched.RichSparseVector
//import com.simplrtek.enriched.RichVector

object Implicits {
  implicit def enrichedDenseVector(vector:DenseVector)= new RichDenseVector(vector)
  implicit def enrichedSparseVector(vector:RandomAccessSparseVector)= new RichSparseVector(vector)
  //implicit def enrichVector(vector:Vector) = new RichVector(vector)
  
}