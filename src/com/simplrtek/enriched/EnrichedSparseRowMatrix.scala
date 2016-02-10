package com.simplrtek.enriched

import org.apache.mahout.math.function.VectorFunction
import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import java.io.File
import org.apache.commons.lang3.exception.ExceptionUtils 
import org.apache.mahout.math._
import scalabindings._
import RLikeOps._
import drm._


class EnrichedSparseRowMatrix(matrix : SparseRowMatrix){
  
  
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
      def apply(v : Vector):Double={
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
     * Load a Sparse Matrix from a file.
     * @param		f		The file to load from
     * @return	The SparseRowMatrix
     */
    def load(f:File):SparseRowMatrix={
        Pickler.unpickleFrom[SparseRowMatrix](f)   
    }
    
    /**
     * Save the matrix to a file.
     * @param		The file to save to.
     */
    def save(f:File)={
      Pickler.pickleTo(matrix, f)
    }
    
    /**
     * Return the matrix as a Sparse Matrix
     * @return		A SparseMatrix of the original matrix.
     */
    def toSparseMatrix():SparseMatrix={
      var m2:SparseMatrix = new SparseMatrix(matrix.numRows(),matrix.numCols())
      
      val it = matrix.iterator()
      while(it.hasNext()){
        val el = it.next()
        m2.assignRow(el.index(), el.vector())
      }
      
      m2
    }
    
    /**
     * Creates a dense matrix from the original matrix.
     * @return		A dense matrix of the original matrix.
     */
    def toDense():DenseMatrix={
      var m2:DenseMatrix = new DenseMatrix(matrix.numRows(),matrix.numCols())
      
      val it = matrix.iterator()
      while(it.hasNext()){
        val el = it.next() 
        m2.assignRow(el.index(), el.vector())
      }
      
      m2
    }
    
    /**
     * Stack two matrices on top of each other. Iterators are created from the matrices
     * and a new matrix must be returned.
     * 
     * @param		matrix							The SparseRowMatrix to stack to
     * @param		{Boolean}{prior}		Whether to stack before or after the original matrix.
     * @return	A stacked sparse row matrix.
     */
    def vStack(mat: SparseRowMatrix, prior:Boolean = false):SparseRowMatrix={
      if(mat.numCols() != matrix.numCols()){
        try{
          throw new ArrayIndexOutOfBoundsException("Number of Columns Must Match")
        }catch{
          case t:Throwable => println(t.getMessage +"\n"+ExceptionUtils.getStackTrace(t))
        }
      }
      
      var m2:SparseRowMatrix = new SparseRowMatrix(matrix.numRows() + mat.numRows(),matrix.numCols())
      
      var it = if(prior) mat.iterator() else matrix.iterator()
      while(it.hasNext()){
        val el = it.next()
        m2.assignRow(el.index(), el.vector())
      }
     
      var currSize = matrix.numRows()
      it = if(prior) matrix.iterator() else mat.iterator()
      while(it.hasNext()){
        val el = it.next()
        m2.assignRow(currSize + el.index(), el.vector())
      }
      
      m2
    }//stackMatrix
    
    
    /**
     * Stack the sparse matrix to the existing matrix horizontally. The matrices
     * must have an equal number of rows.
     * 
     * @param			matrix							The matrix to stack horizontally.
     * @param			{Boolean}{prior}		Whether to stack the 
     */
    def hStack(mat: SparseRowMatrix, prior:Boolean = false):SparseRowMatrix={
      if(mat.numRows() != matrix.numRows()){
        try{
          throw new ArrayIndexOutOfBoundsException()
        }catch{
          case t:ArrayIndexOutOfBoundsException => println(t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        }
      }
      
      val cols:Integer = mat.numCols() + matrix.numCols()
      var m2:SparseRowMatrix = new SparseRowMatrix(matrix.numRows(),cols)
      
      var m1P = if(prior) mat else matrix
      var m2P = if(prior) matrix else mat
      
      for(i <- 0 to matrix.numRows()){
        for(j <- 0 to cols){
          if(j >= m1P.numCols()){
            var col = j - matrix.numCols()
            m2.set(i, col , m2P.get(i, col))
          }else{
            m2.set(i, j, m1P.get(i, j))
          }
        }
      }
      
      m2
    }//hstack
  }