package com.simplrtek.enriched

import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import java.io.File

class EnrichedSparseMatrix(matrix:SparseMatrix){
    
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