package com.simplrtek.enriched

import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import com.simplrtek.enriched.RichVector._
import java.io.File

/**
 * A matrx class that includes some numpy like functions for use with single core
 * tasks and betterment of others. 
 */
object RichMatrix {
  
  //implicit enrichment
  implicit def richDenseMatrix(matrix:DenseMatrix) = new EnrichedDenseUtils(matrix)
  implicit def richSparseMatrix(matrix:SparseMatrix) = new EnrichedSparseUtils(matrix)
  implicit def richSparseRowMatrix(matrix:SparseRowMatrix) = new EnrichedSparseRowUtils(matrix)
 
  
  //enrichment classes
  implicit class EnrichedDenseUtils(matrix:DenseMatrix){
    
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
      var mat2:DenseMatrix = new DenseMatrix(matrix.numRows,matrix.numCols)
      var matpointer:DenseMatrix = null
      if(prior){
        matpointer= matrix2
      }else{
       matpointer = matrix
      }
      
      for(i <- 0 to matpointer.numRows()){
        
      }
      
      //avoid array of pointers with its larger overhead
      if(prior){
        matpointer = matrix
      }else{
        matpointer = matrix2
      }
      
      for(i <- 0 to matpointer.numRows()){
        
      }
      
      null
    }//vstack
    
    /**
     * Horizontally stack all of the rows from a matrix to this matrix requires rewriting.
     * 
     * @param		matrix		The dense matrix to stak
     * @param		prior			Whether to stack the provided matrix before the current matrix.  
     */
    def hstack(matrix:DenseMatrix,prior:Boolean = false):DenseMatrix={
      null
    }//hstack
    
  }
  
  implicit class EnrichedSparseUtils(matrix:SparseMatrix){
    
    def load(f:File):SparseMatrix={
      null
    }//load
    
    def save(f:File)={
      
    }//save
    
    def toCSRMatrix():SparseRowMatrix={
      null
    }
    
    def toDense():DenseMatrix={
      null
    }//todense
    
    def stackMatrix(matrix:SparseMatrix,prior:Boolean = false):SparseMatrix={
      null
    }//stackMatrix
    
    def vstack(matrix:SparseMatrix, prior:Boolean = false):SparseMatrix={
      null
    }//vstack
    
    def hstack(matrix:SparseMatrix, prior:Boolean = false):SparseMatrix={
      null
    }//hstack
    
  }
  
  implicit class EnrichedSparseRowUtils(matrix : SparseRowMatrix){
    
    def load(f:File):SparseRowMatrix={
        null   
    }//load
    
    def save(f:File)={
      
    }//save
    
    def toSparseMatrix():SparseMatrix={
      null
    }//toSparseMatrix
    
    def toDense():DenseMatrix={
      null
    }//toSparse
    
    def stackMatrix(matrix: SparseRowMatrix, prior:Boolean = false):SparseRowMatrix={
      null
    }//stackMatrix
    
    def vstack(matrix: SparseRowMatrix, prior:Boolean = false):SparseRowMatrix={
      null
    }//vstack
    
    def hstack(matrix: SparseRowMatrix, prior:Boolean = false):SparseRowMatrix={
      null
    }//hstack
  }
}