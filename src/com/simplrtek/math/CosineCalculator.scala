package com.simplrtek.math

import breeze.linalg.norm
import breeze.linalg.Transpose
import breeze.linalg.SparseVector
import breeze.linalg.{CSCMatrix,Matrix,DenseVector}

object CosineCalculator{
  
  
  /**
   * Calculate the cosines between 2 vectors (uses staright API)
   * 
   * @param		documenta			Sparse Vector a
   * @param		documentb			Sparse vector b
   * 
   * @return 	The cosine similarity as a double.
   */
  def getSparseCosines(documenta : SparseVector[Double],documentb : SparseVector[Double]):Double={
    documenta.dot(documentb) / (norm(documenta) * norm(documentb))
  }
  
  /**
   * Calculate cosine similarities for several different matrices
   * 
   *@param		documents				The first CSCMatrix
   *@param    vec							The second CSCMatrix
   * 
   *@return		vec.t * documents / (linalgnorm(vec)*linalgnorm(documents))
   */
  def calcCosineSimilarity(documents : CSCMatrix[Double], vec : CSCMatrix[Double])={
    var vals = vec.t * documents
    var d : Double  = 0
    for(v <- documents.activeValuesIterator){
      d += (v * v)
    }
    
    var d2 : Double = 0
    for(v <- vec.activeValuesIterator){
      d2 += (v*v)
    }
    
    d = d * d2
    vals.mapActiveValues { x => x / d }
  }
  
  /**
   * Calculate Cosines of all documents in the matrix using a faily fast implementation.
   * 
   * @param				documents				The CSC matrix to calculate cosines with itself
   * @return			documents.t * documents / (linalgnorm(documents)^2)
   */
  def getCosines(documents : CSCMatrix[Double]):Matrix[Double]={
    var vals = documents.t * documents
    var d : Double  = 0
    for(v <- documents.activeValuesIterator){
      d += (v * v)
    }
    d = d * d
    vals.mapActiveValues { x => x / d }
  }
}