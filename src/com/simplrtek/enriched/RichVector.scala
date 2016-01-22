package com.simplrtek.Driver

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import java.lang.ArrayIndexOutOfBoundsException
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import scala.collection.JavaConverters._
import com.simplrtek.pickle.Pickler
import org.apache.mahout.math._
import scalabindings._
import RLikeOps._
import drm._


  /**
   * Some functions are missing from the vector interface likely for
   * compatibility across all vectors.
   */
 class RichVector(vector:Vector){
    
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
     * Copy data from another vector into the existing vector (overriding existing data)
     * @param		src					The vector to copy from
     * @param		srcpos			The position from which to start copying from
     * @param		destpost		The destination position to start at in the original vector
     * @param		length			The length to copy
     * @return	The original vector with the data copied to it. (functional language)
     */
    def copyTo(src:Vector,srcpos:Integer,destpos:Integer,length:Integer):Vector={
      if(srcpos + length > src.size || destpos + length > vector.size){
        try{
          throw new ArrayIndexOutOfBoundsException("The destination or source positions must not drop off the end of the vector")          
        }catch{
          case e:ArrayIndexOutOfBoundsException => e.getMessage +"\n"+ExceptionUtils.getStackTrace(e)
        }
      }
      
      for(i <- 0 to length){
        vector.set(destpos + i, src.get(srcpos + i))
      }
      
      vector
    }
    
    /**
     *Takes a list and appends it to the existsing doubles vector without conversion to array.
     * 
     * @param		data		The  list to append to the vector. All data must convert to doubles.
     * @return	A vector composed of the existing vector with the data appended.
     */
    def appendList[T](data:List[T]):Vector={
      var v:Vector = new DenseVector(vector.size+data.size)
      v.assign(vector)
      for(i <- 0 to data.size){
        v.set(vector.size + i, data(i).asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Takes an array and appends it to the existing doubles vector.
     * 
     * @param			data		The data to append to the vector (must be convertable to doubles)
     * @return		A vector composed of the existing vector with the data appended.
     */
    def appendArray[T](data: Array[T]):Vector={
      
      if(data.length == 0){
        return vector
      }
      
      var v:Vector = new DenseVector(vector.size()+data.length)
      v.assign(vector)        
      
      for(i <- 0 to data.length){
        v.set(vector.size()+i, data(i).asInstanceOf[Double])//must be convertable
      }
      
      v
      
    }
    
    /**
     * Append a dense vector to this vector. Only works with dense vectors.
     * 
     * @param		v2		The vector or dense vector to append
     * @return	A vector composed of the original with this vector appended.
     */
    def appendVector(v2:Vector):Vector={
      var v:Vector = new DenseVector(vector.size()+v2.size())
      v.assign(vector)
      
      var sz:Integer =0
      
      for(i <- 0 to v2.size()){
        v.set(vector.size()+i, v2.get(i))
      }
      
      v
    }
    
    /**
     * Convert generic vector to an array.
     * @return		The vector as an array of doubles.
     */
    def asArray():Array[Double]={
      var arr:Array[Double] = Array[Double]()
      for(i <- 0 to vector.size()){
        arr = arr :+ vector.get(i)
      }
      arr
    }//asArray
    

  }