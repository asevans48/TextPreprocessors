package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import java.io.File
import scala.collection.JavaConverters._
import com.simplrtek.pickle.Pickler
import java.lang.ArrayIndexOutOfBoundsException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.mahout.math._
import scalabindings._
import RLikeOps._
import drm._

import org.apache.mahout.math.function.VectorFunction


class RichDenseVector(vector:DenseVector){
  
    
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
    
    def reduceVector():Double={
      this.vector.zSum
    }
    
    /**
     * Get the number of non-zero elements
     */
    def getNNZ():Int={
      this.vector.getNumNonZeroElements
    }
  
  
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
     * @param			data			Specified array of data with the same length as indices
     * @return		The random access sparse vector created from the indices and data.
     */
    def buildFromArr[T](data:Array[T]):DenseVector={
      new DenseVector(data.asInstanceOf[Array[Double]])
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
    def buildFromList[T](data:List[T]):DenseVector={
      new DenseVector(data.asInstanceOf[List[Double]].toArray)
    }
    
    /**
     * Append data to the vector from a dense array
     */
    def appendArray[T](data:Array[T]):DenseVector={
      var sz:Integer = vector.size()
      var v:DenseVector = new DenseVector(data.length + vector.size())
      val it = vector.iterator()      
      var i = 0
      
      while(it.hasNext()){
        v.set(i, it.next().get)
        i += 1
      }
      
      for(j <- 0 to data.length){
        v.set(i+j, data(j).asInstanceOf[Double])
      }
      
      v
    }
    
    /**
     * Append a dense vector to a sparse vector.
     */
    def appendDenseVector(v:DenseVector):DenseVector={
      var sz:Integer = vector.size()
      var v2:DenseVector = new DenseVector(vector.size() + v.size())
      
      var it = vector.iterator()
      var i = 0
      while(it.hasNext()){
        v2.set(i,it.next().get)
        i += 1
      }
      
      it = v.iterator()
      while(it.hasNext()){
        v2.set(i, it.next().get)
        i += 1
      }
      

      v2
    }
    
    /**
     * Append a sparse vector to this vector
     */
    def appendSparseVector(v:RandomAccessSparseVector):DenseVector={
      var v2:DenseVector = new DenseVector(v.size() + vector.size())
      v2.assign(0.0)
      var it = vector.iterator()
      var i =0 
      while(it.hasNext()){
        v2.set(i, it.next().get)
        i += 1
      }
      
      it = v.iterator()
      while(it.hasNext()){
        val el = it.next()
        v2.set(i + el.index(), el.get)
      }
      
      v2
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
    def copyToVector(src:DenseVector,srcpos:Integer,destpos:Integer,length:Integer):DenseVector = {
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
    
    
    /**
     * Convert the vector to an array of doubles.
     * @return		An array of double values of the current data.
     */
    def toArray():Array[Double]={
      
      var it = vector.iterator()
      var arr:Array[Double] = Array[Double]()
      while(it.hasNext()){
        arr = arr :+ it.next().get
      }
      arr
    }
    

}