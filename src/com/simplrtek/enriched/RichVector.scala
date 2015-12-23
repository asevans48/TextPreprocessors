package com.simplrtek.enriched

import org.apache.mahout.math.{RandomAccessSparseVector,DenseVector,Vector}
import java.lang.ArrayIndexOutOfBoundsException
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File

import com.simplrtek.pickle.Pickler

/**
 * Attempts to add numpy like operations to mahout for ease of conversion and programming.
 * Includes some appending, saving (pickling with java), and conversions.
 */
object RichVector{
  implicit def enrichSparseVector(vector:RandomAccessSparseVector)= new EnrichedSparseUtils(vector)
  implicit def enrichDenseVector(vector:DenseVector) = new EnrichedDenseUtils(vector) 
  implicit def enrichVector(vector:Vector) = new EnrichedVectorUtils(vector)
  
  /**
   * Some functions are missing from the vector interface likely for
   * compatibility across all vectors.
   */
  implicit class EnrichedVectorUtils(vector:Vector){
    
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
  
  
  implicit class EnrichedDenseUtils(vector:DenseVector){
    
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
     * Build a dense vector from a dense array of data. The data array should 
     * contain all vecetorizable data that convert to doubles.
     * 
     * @param		data		The data to turn into a dense vector.
     * @return	A dense vector ofdoubles.
     */
    def buildFromArr[T](data:Array[T]):DenseVector={
      var v:DenseVector = new DenseVector(data.length)
      for(i <- 0 to data.length){
        v.set(i, data(i).asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Build a dense vector from a list of data.
     * 
     * @param			data		The list of data which should easily convert to a double.
     * @return		A dense vector of doubles.		
     */
    def buildFromList[T](data:List[T]):DenseVector={
      var v:DenseVector = new DenseVector(data.length)
      for(i <- 0 to data.length){
        v.set(i, data(i).asInstanceOf[Double])
      }
      v
    }
    
    /**
     * Append an array to the vector. Returns a new vector
     * so use with care. Numpy equivalent would be hstack.
     * 
     * @param		data		The data to append to the vector.
     * @return	A dense vector of the vector concatenated with the array.
     */
    def appendArr[T](data:Array[T]):DenseVector={
      var currIndex:Integer = 0
      var v:DenseVector = new DenseVector(data.length+vector.size())
      var it = vector.iterator()
      
      while(it.hasNext()){
        v.set(currIndex, it.next().get.asInstanceOf[Double])
        currIndex += 1
      }
      
      currIndex = 0
      
      while(currIndex < data.length){
        v.set(currIndex, data(currIndex).asInstanceOf[Double])
        currIndex += 1
      }
      
      v
    }
    
    /**
     * Append a list to the vector.
     * 
     * @param		data		Append this list to an existing vector
     * @return 	A concatenated and new dense vector.
     */
    def appendList[T](data:List[T]):DenseVector={
      var currIndex:Integer = 0
      var v:DenseVector = new DenseVector(data.length+vector.size())
      var it = vector.iterator()
      
      while(it.hasNext()){
        v.set(currIndex, it.next().get.asInstanceOf[Double])
        currIndex += 1
      }
      
      currIndex = 0
      
      while(currIndex < data.length){
        v.set(currIndex, data(currIndex).asInstanceOf[Double])
        currIndex += 1
      }
      v 
    }
    
    /**
     * Concatenate current vector with another vecor and return new result.
     * 
     * @param		v2		The vector to append.
     * @return	A concatenated and new dense vector. 	
     */
    def appendVector[T](v2 : DenseVector):DenseVector={
      var v : DenseVector = new DenseVector(vector.size() + v2.size())
      v.assign(vector)
      var sz:Integer = vector.size()
      
      for(i <- 0 to v2.size()){
        v.set(sz + i, v2.get(i).asInstanceOf[Double])
      }
      
      v
    }
    
    /**
     * Copy from a different vector into the current vector.
     * 
     * @param		src					The vector to copy from.
     * @param		srcpos  		The position in the source vector to start from
     * @param		destpos			The position in the current vector to insert into
     * @param		length			The number of positions in the source pos to get
     * @return	A new dense vector
     */
    def vectorCopy(src:DenseVector,srcpos:Integer,destpos:Integer,length:Integer):DenseVector={
      
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
     * Convert the vector to an array.
     * @return	The dense Array containing all of the doubles
     */
    def asArray():Array[Double]={
      var dArr:Array[Double] = Array[Double]()
      for(i <- 0 to vector.size()){
        dArr = dArr :+ vector.get(i)
      }
      dArr
    }
    
  }
  
  implicit class EnrichedSparseUtils(vector:RandomAccessSparseVector){
    
    
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
    def appendArray[T](data:Array[T]):RandomAccessSparseVector={
      var sz:Integer = vector.size()
      
      for(currIndex <- 0 to data.length){
        vector.set(sz + currIndex, data(currIndex).asInstanceOf[Double])
        
      }
      vector
    }
    
    /**
     * Append a dense vector to a sparse vector.
     */
    def appendDenseVector(v:DenseVector):RandomAccessSparseVector={
      var sz:Integer = vector.size()
     
      for(currIndex <- 0 to v.size()){
        vector.set(sz + currIndex, vector.get(currIndex).asInstanceOf[Double])
      }
      vector
    }
    
    /**
     * Append a sparse vector to this vector
     */
    def appendSparseVector(v:RandomAccessSparseVector):RandomAccessSparseVector={
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
    def copyToSparseVector(src:DenseVector,srcpos:Integer,destpos:Integer,length:Integer):RandomAccessSparseVector = {
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
  
}