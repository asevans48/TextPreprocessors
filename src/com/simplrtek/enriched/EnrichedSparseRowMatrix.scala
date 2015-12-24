package com.simplrtek.enriched

import org.apache.mahout.math.{SparseMatrix,SparseRowMatrix}
import org.apache.mahout.math.DenseMatrix
import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import com.simplrtek.pickle.Pickler
import java.io.File

class EnrichedSparseRowMatrix(matrix : SparseRowMatrix){
    
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