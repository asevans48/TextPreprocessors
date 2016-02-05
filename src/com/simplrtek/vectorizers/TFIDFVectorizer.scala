package com.simplrtek.vectorizers

import com.simplrtek.enriched.breeze.Implicits._
import breeze.linalg.{CSCMatrix,Matrix}
import breeze.linalg.max
import breeze.linalg.*
import breeze.numerics._
import scala.concurrent.{Future,Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.VectorBuilder
import scala.util.{Success,Failure}
import com.simplrtek.hashing.FeatureHasher

/**
 * This class takes in the hash arrays from the FeatureHasher
 * class and uses the lists to build a tfidf matrix. This 
 * would be faster than converting to a breeze matrix first.
 */
class TFIDFVectorizer(batchSize : Int = 100, duration : Duration = Duration.Inf){
  var docTermCounts : List[Double] = List[Double]()
  var maxFreqs : List[Double] =  List[Double]()
  var tf : List[Double] = List[Double]()
  var idf : List[Double] = List[Double]()
  var hasher : FeatureHasher = _
  var tfidfMatrix : CSCMatrix[Double] = _
  
  def setHasher(hasher : FeatureHasher)={
    this.hasher = hasher
  }
  
  def fit(featureClass : FeatureHasher)={
    this.hasher = featureClass
  }
  
  def getMax(slice : List[Double] ):Future[Double]=Future{
     slice.max
  }//getMax
  
  def getMaxFreqs()={
     maxFreqs = List[Double]()
     var start = 0
     var futs : List[Future[Double]] = List[Future[Double]]()
     for(i <- 0 until this.hasher.vptrs.size){
       futs = futs :+ getMax(this.hasher.values.slice(start, this.hasher.vptrs(i)))
       
       if(futs.size == this.batchSize || (i + 1) == this.hasher.vptrs.size){
         val v = Await.ready(Future.sequence(futs), duration).value.get
         
         v match{
           case Success(t) =>{
             maxFreqs = maxFreqs ++ t
           }
           case Failure(e) =>{
             println("Failed to Get Maximum Freqs")
           }
         }
         
       }
     }
    
  }//getMaxFreqs
  
  def getDocTermCount(slice : List[Double]):Double={
    slice.map { x => if(x > 0) 1 else 0}.sum
  }//getDocTermCount
  
  def calculateDocTerms()={
    docTermCounts = List[Double]()
    var start : Int = 0
    var futs : List[Future[Double]] = List[Future[Double]]()
    for(i <- 0 until this.hasher.vptrs.size){
       futs = futs :+ getMax(this.hasher.values.slice(start, this.hasher.vptrs(i)))
       
       if(futs.size == this.batchSize || (i + 1) == this.hasher.vptrs.size){
         val v = Await.ready(Future.sequence(futs), duration).value.get
         
         v match{
           case Success(t) =>{
             docTermCounts = docTermCounts ++ t
           }
           case Failure(e) =>{
             println("Failed to Get Maximum Freqs")
           }
         }
         
       }
     }
  }//calculateDocTerms
  
  def calcTF(list : (Int,List[Double])):Future[(Int,List[Double])]=Future{
    var start : Int = 0
    
    for(i <- 0 until list._2.size){
        list._2.updated(i , 0.5 + 0.5 * (list._2(i) / this.maxFreqs(i)))
        start += 1
    }
    
    list
  }//calcTF
  
  def getTFMat()={
    var start : Int = 0
    var futs : List[Future[(Int,List[Double])]] = List[Future[(Int,List[Double])]]()
    for(i <- 0 until this.hasher.vptrs.size){
       futs = futs :+ calcTF((start,this.hasher.values.slice(this.hasher.vptrs(start), this.hasher.vptrs(i))))
       
       if(futs.size == this.batchSize || (i + 1) == this.hasher.vptrs.size){
         val v = Await.ready(Future.sequence(futs), duration).value.get
         
         v match{
           case Success(t) =>{
             t.foreach({
               l =>
                 var start : Int = if((i - 1) >= 0) i - 1 else 0
                 while(start < l._1){
                   this.hasher.values.updated(this.hasher.vptrs(start), this.hasher.values(this.hasher.vptrs(start)))
                   start += 1
                 }
             })
             futs = List[Future[(Int,List[Double])]]()
           }
           case Failure(e) =>{
             println("Failed to Get Maximum Freqs")
           }
         }
         
       }
     }
  }//getTFMat
  
   def getIDF( ct : Double, list : (Int,List[Double])):Future[(Int,List[Double])]=Future{
     //(v._1,v._2 *= Math.log(v._3 + 1 / v._4 + 1))
     for( i <- 0 until list._2.size){
       list._2.updated(i, Math.log(ct/list._2(i) ))
     }
     list
   }
   
   def calcIDF()={
      var futs:List[Future[(Int,List[Double])]] = List[Future[(Int,List[Double])]]()
      var i = 0
      val end = batchSize
      while(i < this.hasher.vptrs.size){
           if(i > 0){
             futs = futs :+ getIDF(this.maxFreqs(i),(i,this.hasher.values.slice(this.hasher.vptrs(i-1), this.hasher.vptrs(i))))
           }else{
             futs = futs :+ getIDF(this.maxFreqs(i),(i,this.hasher.values.slice(0, this.hasher.vptrs(i))))  
           }
           
           if(futs.length == this.batchSize || (i +1) == this.hasher.vptrs.size){
          
             val r = Await.ready(Future.sequence(futs), duration).value.get
             
             r match{
               case Failure(t) =>{
                 println("Failed to Compute IDF")
                 println(t.getMessage)
               }
               case Success(x) =>{
                 x.foreach({
                   l => 
                     if(l._1 > 0){
                       var j  = this.hasher.vptrs(l._1 - 1) 
                       var i = 0
                       while( j < this.hasher.vptrs(l._1)){
                         this.hasher.values.updated(this.hasher.vptrs(j), l._2(i))
                         j += 1
                         i +=1
                       }
                     }else{
                       
                     }
                 })
                 futs = List[Future[(Int,List[Double])]]()
               }
             }
           }
        i += 1
      }
      
   }//calcIDF
   

}

/**	A TFIDF Vectorizer that Should be Used with the Feature Hasher.
 * 	The goal is for implementation during testing and use on a single 
 * 	node. 
 * 
 * The constructor takes params.
 * 
 * @param		{Int}{batchSize}									The batchsize to use in multi-core processing
 * @param		{Duration}{duration}							The duration to await results
 */
class TFIDFMatrixVectorizer(batchSize : Int  = 100, duration : Duration = Duration.Inf){
  
  private var docTermCount : Vector[Double] = _
  private var maxDocFreqs : Vector[Double] = _ 
  
  /**
   * Get the maximum count from the iterator.
   */
  def getMax(mat : Matrix[Double]):Future[Double]=Future{
    mat.activeValuesIterator.max
  }
  
  
  /**
   * Get maximum counts for each document to be used in TF calculations. 
   * Uses Futures. Sets maxDocFreqs.
   * 
   * @param		freqMat				The CSC frequeny matrix
   * 
   */
  def getMaxFreqs(freqMat : CSCMatrix[Double])={
     val nrows : Int = freqMat.rows
     val ncols : Int = freqMat.cols
     val builder = Vector.newBuilder[Double]
     var start : Int = 0
     var end : Int = batchSize
     
     while(start < freqMat.cols){
       var matrices : List[Matrix[Double]] = List[Matrix[Double]]()
       if(end > freqMat.rows){
        end = freqMat.rows  
       }
       
       for(i <- start until end){
          matrices = matrices :+ freqMat(i to i, 0 until freqMat.cols)
       }
       
       val r = Await.ready(Future.traverse(matrices)(getMax), duration)
       r.value.get match{
         case Success(t) =>{
           t.foreach { 
             d =>
              builder += d 
           }
         }
         case Failure(e) => {
            println("Failed to get Results from Future, Failure!")
            System.exit(-1)
         }
       }
       
       start += batchSize 
       end += batchSize
     }
     
     maxDocFreqs = builder.result
    
  }
  
  /**
   * Sum the column wise document counts
   */
  def calculateDocTerms(freqs : Matrix[Double]):Future[Double]=Future{
    freqs.valuesIterator.map { x => if(x > 0) 1 else 0}.sum
  }
  
  /**
   * Gets Document Term Counts. This builds a vector of all documents
   * containing a term, reducing a future calculation time. Sets docTermCount
   * 
   * @param		freqMat				The frequency matrix
   */
  def getDocTermCount(freqMat : CSCMatrix[Double])={
    var builder = Vector.newBuilder[Double]
    var start : Int = 0
    var end : Int = batchSize
    
    while(start  < freqMat.cols){
      var matrices : List[Matrix[Double]] = List[Matrix[Double]]()
      if(end > freqMat.cols){
        end = freqMat.cols  
      }
      
      for(i <- start until end){
         matrices = matrices :+ freqMat(0 until freqMat.rows,i to i)
      }
      
      Await.ready(Future.traverse(matrices)(calculateDocTerms), duration).value.get match{
        case Success(t) =>{
          t.foreach { d => builder += d }
        }
        case Failure(e) =>{
          println("Failure in Futures for DocTermCount. Failure!")
          println(e.getMessage)
          System.exit(-1)
        }
      }
      start += batchSize
      end += batchSize
    }
    
    docTermCount = builder.result
  }
  
  
  def calcTF(v : (Int,Matrix[Double])):Future[(Int,Matrix[Double])]=Future{
    (v._1,(0.5 * v._2 / maxDocFreqs(v._1)) += 0.5)
  }
  
  /**
   * Uses multi-core processing to perform the tfidf component. Will reset the matrix
   * so must be called after the documents are generated. The frequency matrix is 
   * thus lost to save memory. Uses docTermCounts and maxFreqs as well.
   * 
   * @param			freqMat			The frequency matrix
   */
  def getTFMat(freqMat : CSCMatrix[Double]):CSCMatrix[Double]={
    val nrows = freqMat.rows
    var futs:List[(Int,Matrix[Double])] = List[(Int,Matrix[Double])]()
    
    var i = 0
    val end = batchSize
    while( i < freqMat.cols){
      val slice:Matrix[Double] = freqMat(0 until nrows, i to i)
      futs = futs :+ (i,slice)
      i += 1
      
      if(i == batchSize || i == freqMat.cols){
        val r = Await.ready(Future.traverse(futs)(calcTF), duration).value.get
        
        r match {
          case Success(r) => {
            r.foreach({
               row => 
                 val col = row._1
                 val v = row._2
                 for( j <- 0 until v.rows){
                     freqMat(j,col) = v(j,0)
                 }
            })
            
          }
          case Failure(r) => {
            println("Failed to Get Tf!")
            println(r.getMessage)
            System.exit(-1)
          }
        }
      }
      
    }
    return freqMat
  }
  
  /**
   * Get the IDF multiple per term
   */
  def calcIDF(v : (Int,Matrix[Double],Int,Double)):Future[(Int,Matrix[Double])]=Future{
    (v._1,v._2 *= Math.log(v._3 + 1 / v._4 + 1))
  }
  
  /**
   * Converts TFMat to TFIDF Mat. This uses some previous calculations to transform
   * a TFMAT to a TFIDFMat.
   */
  def getIDF(tfMat : CSCMatrix[Double]):CSCMatrix[Double]={
    val cols = tfMat.cols
    val rows = tfMat.rows
    
    var futs:List[(Int,Matrix[Double],Int,Double)] = List[(Int,Matrix[Double],Int,Double)]()
    var i = 0
    val end = batchSize
    while( i < tfMat.rows){
      val slice:Matrix[Double] = tfMat( i to i, 0 to cols)
      futs = futs :+ (i,slice,rows,this.docTermCount(i))
      i += 1
      
      if(i == batchSize || i == tfMat.cols){
        val r = Await.ready(Future.traverse(futs)(calcIDF), duration).value.get
        
        r match {
          case Success(r) => {
            r.foreach({
               row => 
                 val rowIndex = row._1
                 val v = row._2
                 for( j <- 0 until v.cols){
                     tfMat(rowIndex,j) = v(0,j)
                 }
            })
            
          }
          case Failure(r) => {
            println("Failed to Get Tf!")
            println(r.getMessage)
            System.exit(-1)
          }
        }
      }
      
    }
    tfMat
  }
  
  
  /**
   * Fit and transform the vector.
   */
  def fit_transform(hashMat : CSCMatrix[Double]):CSCMatrix[Double]={
    this.getMaxFreqs(hashMat)
    this.getDocTermCount(hashMat)
    this.getIDF(this.getTFMat(hashMat))
  }//fit_transform
  
}

object TFIDFTester{
  
  
  
}