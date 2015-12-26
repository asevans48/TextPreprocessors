package com.simplrtek.enriched

import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import java.io.File
import org.apache.commons.lang3.exception.ExceptionUtils

class EnrichedDenseMatrix(matrix:DenseMatrix){
    
    /**
     * Convert the dense matrix to an array. (Useful for instantiation of other Matrices)
     * @return		A multi-dimensional array of the data.
     */
    def toArray():Array[Array[Double]]={
      var i = 0
      var sz = matrix.numRows()
      var darr:Array[Array[Double]] = Array[Array[Double]]()
      while(i < sz){
          val v:Vector = matrix.viewRow(i)
          var innerDArr:Array[Double] = Array[Double]()
          for( j <- 0 to v.size()){
            innerDArr = innerDArr :+ v.get(j)
          }//meh
          darr(i) = innerDArr
          i += 1
      }//for speed use while loop
      darr
    }
    
    /**
     * Load from a pickle file using the java object streams.
     * 
     * @param		f		The file to load
     * @return	The Dense Matrix created from the file.
     */
    def load(f:File):DenseMatrix={
      Pickler.unpickleFrom[DenseMatrix](f)
    }
    
    
    /**
     * Save the current matrix to a file
     * 
     * @param		f		The file to save to (recommend to use .pkl or .matrix as file extensions)
     */
    def save(f:File)={
      Pickler.pickleTo[DenseMatrix](matrix, f)
    }
    
    /**
     * Convert the current matrix to the CSR (Sparse Row Matrix) file format.
     * Requires 2x size of matrix as memory for a max memory use but likely 
     * less.
     * 
     * @return		A Sparse Row Matrix
     */
    def toCSRMatrix():SparseRowMatrix={
      var smatrix:SparseRowMatrix = new SparseRowMatrix(matrix.numRows(),matrix.numCols())
      var i = 0 
      while(i < matrix.numRows()){
        val v:Vector = matrix.viewRow(i)
        for(j <- 0 to v.size()){
          if(v.get(j) != 0){
            smatrix.setQuick(i, j, v.get(j))
          }
        }
        i += 1
      }//while loop for slight speed increase
      smatrix
    }
    
    /**
     * Convert the current matrix to a Sparse Column matrix (breeze-like CSC matrix)
     * @return	A mahout sparse matrix.
     */
    def toSparse():SparseMatrix={
      var smatrix:SparseMatrix = new SparseMatrix(matrix.numRows(),matrix.numCols())
      var i = 0
      while(i < matrix.numRows()){
        val v:Vector = matrix.viewRow(i)
        for(j <- 0 to matrix.numCols()){
          if(v.get(j) != 0){
            smatrix.set(i, j, v.get(j))
          }
        }
        i += 1
      }//actually faster than for loop
      smatrix
    }
    
    /**
     * Stack all of the rows in a matrix to the current matrix, requires rewriting.
     * NOTE: file operations may be useful in the future for a vstack -- look into MahoutVectorWriteable
     * 
     * @param		matrix		The dense matrix to stack to the current matrix.
     * @param		prior			Whether to stack before or after the current matrix.
     * @return	A stacked matrix (brand new so be careful)
     */
    def vstack(matrix2:DenseMatrix,prior:Boolean = false):DenseMatrix={
      
      if(matrix2.numCols() != matrix.numCols()){
        try{
          throw new ArrayIndexOutOfBoundsException("Number of columns in both matrices of vstack must be equal")
        }catch{
          case e:ArrayIndexOutOfBoundsException => println(e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        }
      }
      var mat2:DenseMatrix = new DenseMatrix(matrix.numRows,matrix.numCols)
      
      var it = if(prior) matrix2.iterator() else matrix.iterator()
      
      while(it.hasNext()){
        val el = it.next()
        mat2.assignRow(el.index(), el.vector())
      }
      
      it = if(prior) matrix.iterator() else matrix2.iterator()
      var sz = if(prior) matrix2.numRows() else matrix.numRows()
      
      while(it.hasNext()){
        val el = it.next()
        mat2.assignRow(sz + el.index(), el.vector())
      }
      
      mat2
    }
    
    /**
     * Horizontally stack all of the rows from a matrix to this matrix requires rewriting.
     * 
     * @param		matrix		The dense matrix to stak
     * @param		prior			Whether to stack the provided matrix before the current matrix.  
     */
    def hstack(mat:DenseMatrix,prior:Boolean = false):DenseMatrix={
      if(matrix.numRows() != mat.numRows()){
        try{
          throw new ArrayIndexOutOfBoundsException("The number of rows in both matrices of hstack must match")
        }catch{
          case e:ArrayIndexOutOfBoundsException => println(e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        }
      }
      
      val numCols:Integer = matrix.numCols() + mat.numCols()
      var m2:DenseMatrix = new DenseMatrix(matrix.numRows(),numCols)
      val m1P = if(prior) mat else matrix
      val m2P = if(prior) matrix else mat
      
      for(i <- 0 to matrix.numRows()){
        for(j <- 0 to numCols){
          if(j < m1P.numCols()){
             m2.set(i, j, m1P.get(i, j))
          }else{
            m2.set(i, j - m1P.numCols(), m2P.get(i, j - m1P.numCols()))
          }
        }
      }
      
      m2
    }
    
  }