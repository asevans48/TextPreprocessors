package com.simplrtek.enriched

import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import java.io.File
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.mahout.math._
import org.apache.mahout.math.function.VectorFunction
import scalabindings._
import RLikeOps._
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class EnrichedSparseMatrix(matrix:SparseMatrix){
  
    var batchSize : Integer = 100
  
    /**
      * Get number of non-zero elements in a row / vector
      */
     class NonZeroCounter extends VectorFunction{
        def apply(v : Vector):Double={
          var nz : Double = 0
          val it = v.all.iterator
          while(it.hasNext){
            if(it.next.get != 0.0){
              nz += 1
            }
          }
          nz
        }
     }
  
     
    /**
     * Reduces the rows by summing them
     */
    class RowReducer extends VectorFunction{
      override def apply(v : Vector):Double={
        v.zSum()
      }
    }
    
    def getRowNNZ():Vector={
      this.matrix.aggregateRows(new NonZeroCounter)
    }
    
    def getRowReduction():Vector={
      this.matrix.aggregateRows(new RowReducer)
    }
    
    def getColumnNNZ():Vector={
      this.matrix.aggregateColumns(new NonZeroCounter)
    }
    
    def getColumnReduction():Vector={
      this.matrix.aggregateColumns(new RowReducer)
    }
    
    /**
     * Get the number of non-zero elements
     */
    def getNNZ():Int={
      this.matrix.getNumNondefaultElements.length
    }  
  
  
    /**
     * Load the matrix from a file.
     * 
     * @return a SparseMatrix
     */
    def load(f:File):SparseMatrix={
      Pickler.unpickleFrom[SparseMatrix](f)
    }//load
    
    /**
     * Save the matrix to a file
     */
    def save(f:File)={
      Pickler.pickleTo(matrix, f)
    }
    
    /**
     * Convert to Sparse Row Matrix
     * @return		A sparse row matrix.
     */
    def toSparseRowMatrix():SparseRowMatrix={
      var m2:SparseRowMatrix = new SparseRowMatrix(matrix.numRows(),matrix.numCols())
      
      val it = matrix.iterator()
      while(it.hasNext()){
        val el = it.next()
        m2.assignColumn(el.index(), el.vector())
      }
      
      m2
    }
    
    /**
     * Convert the matrix to a dense matrix.
     * @return		A dense matrix
     */
    def toDense():DenseMatrix={
      var m2:DenseMatrix = new DenseMatrix(matrix.numRows(),matrix.numCols())
      
      for(i <- 0 to matrix.numCols()){
        for(j <- 0 to matrix.numRows()){
          m2.set(j, i, matrix.get(j, i))
        }
      }
      
      m2
    }
    
    /**
     * Vertically stack a matrix. If prior is true do so before appending
     * the old matrix otherwise append new matrix after the old.
     * 
     * @param		matrix							The sparse matrix to append
     * @param		{Boolean}{prior}		Whether to append before the original matrix
     * @return	A new sparse matrix
     */
    def vstack(mat:SparseMatrix, prior:Boolean = false):SparseMatrix={
      if(mat.numCols() != matrix.numCols()){
        try{
          throw new ArrayIndexOutOfBoundsException("The number of columns for both Matrices in VStack Must match")
        }catch{
          case e:ArrayIndexOutOfBoundsException => println(e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        }
      }
      var m2:SparseMatrix = new SparseMatrix(matrix.numRows() + mat.numRows(),matrix.numCols())
      
      var it=if(prior)mat.iterator else matrix.iterator
      var sz:Integer = 0
      while(it.hasNext()){
        val el = it.next()
        m2.assignRow(el.index(), el.vector())
        sz += 1
      }
      
      it= if(prior)matrix.iterator else mat.iterator
      while(it.hasNext()){
        val el = it.next()
        m2.assignRow(sz + el.index, el.vector)
      }
      
      m2
    }
    
    /**
     * Horizontally stack two matrices onto each other. They must have the
     * same number of rows.
     * 
     * @param		matrix		The sparse matrix to stack in addition to the original.
     * @param		prior			Whether to append the new matrix before or after the original.
     * @return	A new SparseMatrix
     */
    def hstack(mat:SparseMatrix, prior:Boolean = false):SparseMatrix={
       if(mat.numRows() != matrix.numRows()){
         try{
           throw new ArrayIndexOutOfBoundsException("Number of Rows in each Hstack must Be Equal")
         }catch{
           case e:ArrayIndexOutOfBoundsException => println(e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
         }
       }
       
       var colSz:Integer = matrix.numCols() + mat.numCols()
       var m2: SparseMatrix = new SparseMatrix(matrix.numRows(),colSz)
       
       val m1P = if(prior) mat else matrix
       val m2P = if(prior) matrix else mat
       
       for(i <- 0 to matrix.numRows()){
         for(j <- 0 to colSz){
           if(j < m1P.numCols()){
             m2.set(i, j, m1P.get(i, j))
           }else{
             m2.set(i, j-m1P.numCols(), m2P.get(i, j-m1P.numCols()))
           }
         }
       }
       
       m2
    }//hstack    
    
}