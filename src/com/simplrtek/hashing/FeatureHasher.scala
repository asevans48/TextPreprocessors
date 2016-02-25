package com.simplrtek.hashing

import com.simplrtek.tokenizers.CountTokenizer
import breeze.linalg._
import breeze.linalg.CSCMatrix
import com.simplrtek.enriched.breeze.Implicits.enrichSparseMatrix
import scala.collection.mutable.ArrayBuffer

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
             val value = Math.abs(tup._2)
             val h = Math.abs(Hash.murmurHashString(w))
             val index = h % this.features
             
            if(words2.contains(index) && !words2.get(index).get.equals(tup._1)){ //safer than using put
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
      
      var hashes = ctMap.map({x => Hash.murmurHashString(x._1)})
      ndocs += 1
      var row : scala.collection.mutable.ArrayBuffer[(Int,Double)] = scala.collection.mutable.ArrayBuffer[(Int,Double)]()
      var rowWords : scala.collection.mutable.ListBuffer[Int] = scala.collection.mutable.ListBuffer[Int]()
      
      for(ctup <- hashes.zip(ctMap).toList.sortBy(f => f._1)){
        var tup = ctup._2
        var value : Double = Math.abs(tup._2.toDouble)
        var hash = ctup._1
        var index = Math.abs(hash) % this.features
        mx = Math.max(mx, index)
        ndocs += 1
        
        if(words.contains(index) && !words.get(index).get.equals(tup._1)){ //safer than using put
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
              row = row :+ (index,-1*value)
            }
            
           
        }else{
            rowWords :+ tup._1
            words.put(index, tup._1)
            row = row :+ (index,value)
        }
      
      }
      cmr.append(row.toArray)
    }

    Runtime.getRuntime.gc()
    
  }
  
  
  /**
   * Get A CSC Matrix from the Sparse Matrix Representation.
   * 
   * @return		The CSC Double Matrix. Use words to get word indices.
   */
  def getCSCMatrix():CSCMatrix[Double]={
    var vptrs: scala.collection.mutable.ArrayBuffer[Int] = scala.collection.mutable.ArrayBuffer[Int]()
    var indices: scala.collection.mutable.ArrayBuffer[Int] = scala.collection.mutable.ArrayBuffer[Int]()
    var values : scala.collection.mutable.ArrayBuffer[Double] = scala.collection.mutable.ArrayBuffer[Double]()

    var currptr = 0
    for(i <- 0 until cmr.size){
      
       vptrs = vptrs :+ currptr
       currptr = vptrs(i) + cmr(i).size

      for(ctup <- cmr(i)){
        indices = indices :+ ctup._1
        values = values :+ ctup._2
      }
    }

    val csc = new CSCMatrix(values.toArray,mx+10 ,vptrs.size - 1 ,vptrs.toArray,indices.toArray)
    csc
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
class FeatureHasher(nfeats : Int = 500000){
 
  var vptrs: Array[Integer] = null
  var indices:Array[Integer] = null
  var values: Array[Double] = null
  var ndocs : Int = 0
  var mx : Int = 0
  var words : scala.collection.mutable.Map[Int,String] = scala.collection.mutable.Map[Int,String]()
  

  /**
   * Converts a List of String,Count Pairs to a Sparse Matrix
   * using the hashing trick. Uses Mahout and the Implicits to
   * build the matrix. The algorithm uses the Hashing Trick.
   * 
   * @param		counts										A list of document counts
   * @param		partial										The boolean stating whether to only partially transform the document.
   */
  def transform(counts:List[Map[String,Integer]])={
    var size:Integer = 0
    val totalfeats = counts.map({x => x.size}).sum
    
     vptrs = Array.fill[Integer](totalfeats)(0)
     indices = Array.fill[Integer](totalfeats)(0)
     values = Array.fill[Double](totalfeats)(0.0)
     ndocs = counts.size
     mx = 0
    
    
    for(mi <- 0 until counts.size){
      var map = counts(mi).map({x => Hash.murmurHashString(x._1)}).zip(counts(mi)).toList.sortBy(f => f._1)

      for(ctup <- map){
        val tup = ctup._2
        var value = tup._2
        if(value > 0){
          var hash = ctup._1
          var index = Math.abs(hash) % totalfeats
     
          mx = Math.max(mx,index)
          value = Math.abs(value)
          if(words.contains(index) && !words.get(index).get.equals(tup._1)){ //safer than using put
            value = -1 * value
          }else if(!words.contains(index)){
            words.put(index, tup._1)
          }
          
          indices(size) = index.asInstanceOf[Integer]
          values(size) = value.doubleValue()
          size += 1
          
          
        }
      }
      if(mi + 1 < vptrs.length){
        vptrs(mi + 1) =  size
      }
    
    }
  }
  
  def getCSCMatrix()={
    
    if(vptrs.size ==0){
      throw new Exception("Count maps must have data. Size of row pointers is 0")
    }

    new CSCMatrix(values.toArray,mx + 500,ndocs,vptrs.map { x => x.toInt },indices.map { x => x.toInt })
  }
}