package com.simplrtek.preprocessors

import scala.collection.JavaConversions._

import java.io.File
import org.apache.mahout.cf.taste.model.DataModel
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity
import org.apache.mahout.cf.taste.impl.neighborhood._
import org.apache.mahout.cf.taste.recommender.RecommendedItem
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity
import org.apache.mahout.cf.taste.neighborhood._
import org.apache.mahout.cf.taste.similarity.UserSimilarity
import org.apache.commons.lang.exception.ExceptionUtils

import org.apache.mahout.vectorizer.DictionaryVectorizer
import org.apache.mahout.vectorizer.DocumentProcessor
import org.apache.mahout.vectorizer.common.PartialVectorMerger
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.SequenceFile
import org.apache.hadoop.io.Text
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.mahout.vectorizer.DocumentProcessor
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Writable
import org.apache.mahout.math.VectorWritable
import sbt.io.IO

object RecPrinter {
  def print(recommendations: Iterable[RecommendedItem],itemId:Long = -1) {
    recommendations.foreach { r =>
      var ol:String = ""
      if(itemId > 0){
        ol += "["+itemId+"]"
      }
      ol+="[%3d] -> %.2f".format( r.getItemID, r.getValue)
      println(ol)
    }
  }
}

class MahoutTest(inputDir:String){
  
  def readSequenceFile(fs:FileSystem,conf:Configuration,writable:Writable,path:String,file:String,outputdir:String = "data/")={
    val vectorsFolder:Path = new Path(new File(outputdir,path).getAbsolutePath)
    val reader:SequenceFile.Reader = new SequenceFile.Reader(fs,new Path(vectorsFolder,file),conf)
    val key:Text = new Text()
    val value:Writable = writable
    
    while(reader.next(key,value)){
      if(writable.isInstanceOf[LongWritable]){
        println("%12s  %d\n".format(key.toString(), (value.asInstanceOf[LongWritable]).get()))
      }else if(writable.isInstanceOf[VectorWritable]){
        println("%12s  %s\n".format(key.toString(), ((value.asInstanceOf[VectorWritable]).get().asFormatString())))
      }
    }
    reader.close
  }
  
  def processTfIdf(conf:Configuration,outputdir:String,tokenizedPath:Path)={
     var sequential:Boolean = false
     var named:Boolean = false
     
     var wordCount = new Path(outputdir)
     var tfidf:Path = new Path(new File(outputdir,"tfidf").getAbsolutePath)
     var tfVectors = new Path(new File(outputdir,DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER).getAbsolutePath)
     DictionaryVectorizer.createTermFrequencyVectors(tokenizedPath,wordCount,DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER, conf, 2, 1, 0.0f, PartialVectorMerger.NO_NORMALIZING, false, 1, 100, sequential, named)
     val docFrequenciesFeatures = TFIDFConverter.calculateDF(tfVectors, tfidf, conf, 100)
     TFIDFConverter.processTfIdf(tfVectors,tfidf, conf, docFrequenciesFeatures, 1, 99, 2.0f, true, sequential, named, 1)
  }
  
  def tokenizeDocuments(fs:FileSystem,conf:Configuration,seqPath:Path,tokenizedPath:Path)={
    val writer: SequenceFile.Writer = new SequenceFile.Writer(fs, conf, seqPath, classOf[Text], classOf[Text])
    
    IO.listFiles(new File(inputDir)).foreach { x =>
      var id:Integer=0
      val lines=IO.read(x).split("\n").foreach { line =>  
        if(line.trim.length > 0){
          writer.append(new Text(x.getName.replaceAll("\\.","")+id), new Text(line))
          id = id + 1
        }
      }
    }
    writer.close()
    DocumentProcessor.tokenizeDocuments(seqPath, classOf[StandardAnalyzer], tokenizedPath,conf)
  }
  
  def TFIDFTest(outdir:String = "data/")={
    
    val conf:Configuration = new Configuration()
    val fs:FileSystem = FileSystem.get(conf)
    val seqPath:Path = new Path(outdir,"sequence")
    val tokenizedPath:Path = new Path(outdir,DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER)
    //tokenizeDocuments(fs,conf,seqPath,tokenizedPath)
    //processTfIdf(conf,outdir,tokenizedPath)
    readSequenceFile(fs, conf,new LongWritable(), "wordcount", "part-r-00000")
    //readSequenceFile(fs, conf,new VectorWritable(), "tfidf/tfidf-vectors", "part-r-00000")
  }
  
  def RecommendMovies()={
    println("Starting")
    try{
      val datamodel:DataModel = new FileDataModel(new File("data/movies.csv"))
      val recommender:GenericItemBasedRecommender = new GenericItemBasedRecommender(datamodel,new LogLikelihoodSimilarity(datamodel))
      datamodel.getItemIDs.toIterable.foreach { itemId =>
          val recommendations = recommender.mostSimilarItems(itemId, 5)
          RecPrinter.print(recommendations,itemId)
      }
    }catch{
      case e:Throwable => println("ERROR: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
    }
    println("Complete")
  } 
}

object Driver{
  
  def main(args:Array[String])={
      var tst:MahoutTest = new  MahoutTest("E:/SimplrTekPython/com/simplrtek/python/analyzer/data/sents")
      //tst.RecommendMovies()
      tst.TFIDFTest()
  }
}