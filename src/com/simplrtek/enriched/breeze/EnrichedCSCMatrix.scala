package com.simplrtek.enriched.breeze

import breeze.linalg.{CSCMatrix,SparseVector}
import scala.concurrent.{Await,Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.apache.commons.lang3.exception.ExceptionUtils
import scala.collection.mutable.ArrayBuffer

class EnrichedCSCMatrix(matrix : CSCMatrix[Double]) {
  
  /**
   * Slice a series of columns from a CSC Matrix. Start and end columns are zero based.
   * If the indices for the columns are appropriate, returns a copy of these columns
   * no matter what.
   * 
   * @param			start			The starting column (included)
   * @param			end 			The final column (included). 
   * @return		CSCMatrix			A new CSCMatrix of the shape [matrix.rows,start-end columns]
   */
  def sliceCols(start : Int, end : Int):CSCMatrix[Double]={
    if(start < 0 || end > matrix.rows){
        try{
          throw new Exception(s"Indices must be within column $start and column $end") 
        }catch{
          case t : Throwable => println(t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        }
    }
    
    var csc = new CSCMatrix.Builder[Double](rows = matrix.rows,cols = end - start)
    
    
    for(i <- start to end){
      var start = matrix.colPtrs.apply(i)
      var end = if(i + 1 < matrix.cols) matrix.colPtrs(i+1) else matrix.data.length
      
      while(start < end){
        csc.add( matrix.rowIndices(start),i, matrix.data(start))
        start += 1
      }
      
    }
    
    csc.result
  }
  
  
  
  /**
   * Get a row from the matrix and return it as a Sparse Vector
   * 
   * @param			row 		A row from the matrix
   * @return		A sparse vector of the data from the column as a Sparse Vector of matrix.rows in size.
   */
  def getRow(row : Int):SparseVector[Double]={
    var data : ArrayBuffer[Double] = new ArrayBuffer[Double]()
    var indices : ArrayBuffer[Int] = new ArrayBuffer[Int]()
    
    for(col <- 0 to matrix.cols){
      if(matrix.apply(row,col) != 0.0){
        data += matrix.apply(row, col)
        indices += col
      }
    }
    
    new SparseVector(indices.toArray,data.toArray,matrix.cols)
  }
  
  /**
   * Slice  a column from the matrix and return a Sparse Vector
   * 
   * @param		col			The column to slice from
   * @return	A sparse vector of data from the matrix of matrix.rows in size
   */
  def getColumn(col : Int):SparseVector[Double]={
    var data : ArrayBuffer[Double] = new ArrayBuffer[Double]()
    var indices : ArrayBuffer[Int] = new ArrayBuffer[Int]()
    for(row <- 0 until matrix.rows){
      
          
      if(matrix.apply(row,col) > 0.0){
        
        data += matrix.apply(row, col)
        indices += row
      }
    }
    
    new SparseVector(indices.toArray,data.toArray,matrix.cols)
  }
  
  
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
  
 def linalgNorm():Future[Double]=Future{
   var v : Double =0
   matrix.valuesIterator.foreach { x => v = v + x*x }
   Math.sqrt(v)
 }
}