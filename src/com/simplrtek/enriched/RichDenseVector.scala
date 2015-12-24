package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import java.io.File
import scala.collection.JavaConverters._
import com.simplrtek.pickle.Pickler
import java.lang.ArrayIndexOutOfBoundsException
import org.apache.commons.lang3.exception.ExceptionUtils

class RichDenseVector(vector:DenseVector){
    
    /**
     * Load a vector from a file.
     * @param		f		The file to load from.
     * @return	A vector.
     */
    def load(f:File):Vector={
      Pickler.unpickleFrom[Vector](f)
    }
    
    /**
     * Pickle the vector to a file (uses java output streams)
     * 
     * @param		f		The file to pickle to.
     */
    def save(f:File)={
      Pickler.pickleTo(vector, f)
    }
    
    /**
     * Build Sparse Vector From an index list and a data list.
     * @param			indices		Integer array of indices
     * @data			data			Specified array of data with the same length as indices
     * @return		The random access sparse vector created from the indices and data.
     */
    def buildFromArrs[T](indices:Array[Integer],data:Array[T]):RandomAccessSparseVector={
      var v:RandomAccessSparseVector = new RandomAccessSparseVector()
      
      if(indices.length != data.length){
        try{
          throw new ArrayIndexOutOfBoundsException("Indices do not Match data. ")
        }catch{
          case e:ArrayIndexOutOfBoundsException => println(e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
          case t:Throwable => println("Unknown Error.\n"+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        }
      }
      
      for(i <- 0 to indices.length){
        v.set(indices(i), data(i).asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Build a sparse matrix from a map with an (integer index,Double tuple)
     * @param		data			The data in a map with Integer index, data to double pairs
     * @return 	A random access sparse vector.
     */
    def buildFromMap[T](data:Map[Integer,T]):RandomAccessSparseVector={
      var v:RandomAccessSparseVector  = new RandomAccessSparseVector()
      for(tup <- data){
        v.set(tup._1, tup._2.asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Build a Sparse Vector from a dense list
     * 
     * @param		data		The list of data to convert from a dense array to sparse vector
     * @return	A random access sparse vector.
     */
    def buildFromList[T](data:Array[T]):RandomAccessSparseVector={
      var v:RandomAccessSparseVector = new RandomAccessSparseVector(data.size)
      for(i <- 0 to data.size){
        v.set(i, data(i).asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Append data to the vector from a dense array
     */
    def appendArray[T](data:Array[T]):DenseVector={
      var sz:Integer = vector.size()
      
      for(currIndex <- 0 to data.length){
        vector.set(sz + currIndex, data(currIndex).asInstanceOf[Double])
        
      }
      vector
    }
    
    /**
     * Append a dense vector to a sparse vector.
     */
    def appendDenseVector(v:DenseVector):DenseVector={
      var sz:Integer = vector.size()
     
      for(currIndex <- 0 to v.size()){
        vector.set(sz + currIndex, vector.get(currIndex).asInstanceOf[Double])
      }
      vector
    }
    
    /**
     * Append a sparse vector to this vector
     */
    def appendSparseVector(v:RandomAccessSparseVector):DenseVector={
      var sz:Integer = v.size()
      var currIndex = 0
      var it = v.iterator()
      
      while(it.hasNext()){
        var el = it.next()
        vector.set(el.index()+currIndex, el.get)
        currIndex += 1
      }
      
      vector
    }
    
    /**
     * Copy from the src Dense vector into the sparse vector.
     *
     * @param		src					The vector to copy from.
     * @param		srcpos  		The position in the source vector to start from
     * @param		destpos			The position in the current vector to insert into
     * @param		length			The number of positions in the source pos to get
     * @return	A new dense vector
     */
    def copyToSparseVector(src:DenseVector,srcpos:Integer,destpos:Integer,length:Integer):DenseVector = {
      if((destpos + length) > vector.size() || srcpos+length > src.size()){
        try{
          throw new ArrayIndexOutOfBoundsException("Copy will fall off end of vector")
        }catch{
          case e:ArrayIndexOutOfBoundsException => e.getMessage+"\n"+ExceptionUtils.getStackTrace(e) 
        }
      }
      
      for(i <- 0 to length){
        vector.set((destpos + i), src.get(srcpos + i))
      }
      
      vector
    }
    


}