package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer
import breeze.linalg._
import breeze.linalg.CSCMatrix
import com.simplrtek.hashing.Hash
import com.simplrtek.enriched.Implicits._

import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import java.util.ArrayList
import scala.collection.JavaConversions._


/**
 * The parallel Feature Hasher creates a more memory intensive version 
 * of a hasher that must be matched with an appropriately parallel TFIDF
 * or other vectorizer. 
 * 
 * The values are Array[Array[(Int,Double)]] representing rows with sparse
 * index to value mappings. This contrasts to 3 large arrays representing
 * values, indices, and row pointers
 * 
 * 
 */
class ParallelFeatureHasher(total_features : Integer = 500000){
  var features : Int = total_features
  var cmr : scala.collection.mutable.ArrayBuffer[Array[(Int,Double)]] = scala.collection.mutable.ArrayBuffer[Array[(Int,Double)]]()
  var ndocs : Int = 0
  var mx : Int = 0
  var words : scala.collection.mutable.Map[Int,String] = scala.collection.mutable.Map[Int,String]()
  
  /**
   * Resize each row array to match the new features size. This will take 2x memory
   * so please use the total_features variable.
   */
  def resize()={
    this.features *= 3
    var cmr2 =  scala.collection.mutable.ArrayBuffer[Array[(Int,Double)]]()
    var words2 : scala.collection.mutable.Map[Int,String] = scala.collection.mutable.Map[Int,String]()
  
    mx = 0
    
    for(arr <- cmr){
         var row2 : scala.collection.mutable.ArrayBuffer[(Int,Double)] = scala.collection.mutable.ArrayBuffer[(Int,Double)]()
         arr.foreach({
           tup =>
             var rowWords : scala.collection.mutable.ListBuffer[Int] = scala.collection.mutable.ListBuffer[Int]()
             val w = words2.get(tup._1).get
             val value = tup._2
             val h = Hash.murmurHashString(w)
             val index = h % this.features
             
            if(words2.contains(index) && !words2.get(index).equals(tup._1)){ //safer than using put
                if(rowWords.contains(index)){
                  var j =0
                  var run = true
                  while(run && j < row2.size){
                    if(tup._1 == index){
                      row2.update(j, (index,tup._2 - value))
                    }
                    j += 1
                  }
                  
                  if(j == row2.size){
                    row2.append((index,-1*value))
                  }
                }else{
                  rowWords = rowWords :+ index
                  row2.append((index,-1*value))
                }
                
               
            }else{
                rowWords :+ tup._1
                words2.put(index, w)
                row2 = row2 :+ (index,value)
            }
             
         })
         cmr2.append(row2.toArray)    
    }
    words = words2
    cmr = cmr2
  }
  
  /**
   * Converts a List of String,Count Pairs to a Sparse Matrix
   * using the hashing trick. Uses Mahout and the Implicits to
   * build the matrix. The algorithm uses the Hashing Trick.
   * 
   * @param		counts										A list of document counts
   * @param		partial										The boolean stating whether to only partially transform the document.
   */
  def transform(counts:List[Map[String,Integer]],partial : Boolean = false)={
    if(partial == false){
      cmr = scala.collection.mutable.ArrayBuffer[Array[(Int,Double)]]()
      ndocs = 0
      mx = 0
    }else{
      ndocs += counts.size
    }
    
    for(ctMap <- counts){
      
      if(ctMap.size > this.features / 3){
        try{
          throw new Exception("Too Few Features Per Row.")
        }catch{
           case t : Throwable =>{
             println(t.getMessage+"\nNumber of Features must be greater than "+this.features+".Please use the features variable. Resizing!")
           }
           resize()          
        }
      }
      
      ndocs += 1
      var row : scala.collection.mutable.ArrayBuffer[(Int,Double)] = scala.collection.mutable.ArrayBuffer[(Int,Double)]()
      var rowWords : scala.collection.mutable.ListBuffer[Int] = scala.collection.mutable.ListBuffer[Int]()
      
      for(tup <- ctMap){
        var value : Double = tup._2.asInstanceOf[Double]
        var hash = Hash.murmurHashString(tup._1)
        var index = hash % this.features
        mx = Math.max(mx, index)
        ndocs += 1
        
        if(words.contains(index) && !words.get(index).equals(tup._1)){ //safer than using put
            if(rowWords.contains(index)){
              var j =0
              var run = true
              while(run && j < row.size){
                if(tup._1 == index){
                  row.update(j, (index,tup._2 - value))
                }
                j += 1
              }
              
              if(j == row.size){
                row.append((index,-1*value))
              }
            }else{
              rowWords = rowWords :+ index
              row.append((index,-1*value))
            }
            
           
        }else{
            rowWords :+ tup._1
            words.put(index, tup._1)
            row = row :+ (index,value)
        }
      
      }
      cmr.append(row.toArray)
    }
    
  }
  
  
  /**
   * Get A CSC Matrix from the Sparse Matrix Representation.
   * 
   * @return		The CSC Double Matrix. Use words to get word indices.
   */
  def getCSCMatrix():CSCMatrix[Double]={
    var csc = new CSCMatrix.Builder[Double](mx,ndocs)
    
    for(i <- 0 until  cmr.size){
      for(j <- 0 until cmr(i).size){
        csc.add(cmr(i)(j)._1,i,cmr(i)(j)._2)
      }
    }
    
    csc.result
  }
  
}

/**
 * This class contains feature hashing capabilities. Capabilities includes the 
 * use of a basic feature hashing task using multiple arrays to then build a Sparse Matrix. 
 * It also includes a fork join pool version that can potentially reduce the memory footprint
 * and improve speed. It is not  a word count vectorizer. There is a separate class for this. 
 * 
 * 
 * References:
 * https://github.com/scikit-learn/scikit-learn/blob/c9572494a82b364529374aafca15660a7366e2c4/sklearn/feature_extraction/_hashing.py
 * 
 * Hashes Text Features to matrices.
 */
class FeatureHasher(total_features :Integer = 500000){
  var features : Integer = total_features
  var vptrs: List[Integer] = List[Integer]()
  var indices:List[Integer] = List[Integer]()
  var values: ArrayList[Double] = new ArrayList[Double](this.features)
  var ndocs : Int = 0
  var mx : Int = 0
  var words : scala.collection.mutable.Map[Int,String] = scala.collection.mutable.Map[Int,String]()
  

  /**
   * Resize the indcies by growing the number of features.
   * This requires 2x the memory.
   */
  def resize()={
    mx = 0
    var size : Integer = 0
    var vptrs2 : List[Integer] = List[Integer]()
    var indices2 : List[Integer] = List[Integer]()
    var words2 : scala.collection.mutable.Map[Int,String] = scala.collection.mutable.Map[Int,String]()
    this.features *= 3
    var values2 : ArrayList[Double] = new ArrayList[Double](this.features)
    
    var start = 0
    for(i <- 0 until vptrs.size){
       while(start < vptrs(i)){
         val oldIndex = indices(start)
         var value = Math.abs(this.values(start))
         val w = words.get(oldIndex).get
         val hash = Hash.murmurHashString(w)
         val index = hash % this.features
         words2.put(index, w)
         indices2 = indices2 :+ index.asInstanceOf[Integer]
         mx = Math.max(mx,index)
         if(hash < 0){
           value *= -1
         }
         
         values2.set(size, value.doubleValue())
         size += 1
         start += 1
       }
       vptrs2 = vptrs2 :+ size
    }
    
    this.vptrs = vptrs2
    this.indices = indices
    this.values = values2
    this.words = words2
  }

  /**
   * Converts a List of String,Count Pairs to a Sparse Matrix
   * using the hashing trick. Uses Mahout and the Implicits to
   * build the matrix. The algorithm uses the Hashing Trick.
   * 
   * @param		counts										A list of document counts
   * @param		partial										The boolean stating whether to only partially transform the document.
   */
  def transform(counts:List[Map[String,Integer]],partial : Boolean = false)={
    var size:Integer = 0
    
    if(partial == false){
      vptrs = List[Integer]()
      indices = List[Integer]()
      values = new ArrayList[Double](this.features)
      ndocs = counts.size
      mx = 0
    }else{
      ndocs += counts.size
    }
    
    for(map <- counts){
      
      if(map.size >= this.features/3){
            try{
              throw new Exception("Too Few Features Per Row.")
            }catch{
              case t : Throwable =>{
                println(t.getMessage+"\nNumber of Features must be greater than "+this.features+".Please use the features variable. Resizing!")
              }
              resize()          
            }
       }
      
      for(tup <- map){
        
        var value = tup._2
        if(value > 0){
          var hash = Hash.murmurHashString(tup._1)
          var index = Math.abs(hash) % this.features
     
          indices = indices :+ index.asInstanceOf[Integer]
          mx = Math.max(mx,index)
          if(words.contains(index) && !words.get(index).equals(tup._1)){ //safer than using put
            value *= -1
          }else if(!words.contains(index)){
            words.put(index, tup._1)
          }
          
          values.set(size, value.doubleValue())
          size += 1
          
          
        }
      }
      vptrs = vptrs :+ size
    
    }
  }
  
  def getCSCMatrix()={
    
    if(vptrs.size ==0){
      throw new Exception("Count maps must have data. Size of row pointers is 0")
    }

    
    val sm = new CSCMatrix.Builder[Double](mx,ndocs)
    var start:Integer = 0
    for(i <- 0 until vptrs.size){
       while(start < vptrs(i)){
         sm.add(indices(start),i, values(start))
         start += 1
       }
    }
    sm.result
  }
}