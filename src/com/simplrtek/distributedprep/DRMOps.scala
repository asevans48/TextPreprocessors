package com.simplrtek.distributedprep

import org.apache.mahout.sparkbindings.drm._
import org.apache.mahout.math._
import drm._
import org.apache.hadoop.io.SequenceFile
import org.apache.hadoop.conf.Configured
import org.apache.mahout.utils.vectors.io.SequenceFileVectorWriter

object DRMOps {
  
  /**
   * A convenience method so that no more imports are needed. Uses drm.drmDfsRead.
   * 
   * @param 		path			The path to the file in the filesystem.
   * @param			sc				The Spark Context to use.
   * 
   * @return		A CheckpointedDrm
   */
  def loadFromHdfs(path : String)(implicit sc: DistributedContext):CheckpointedDrm[_] ={
    drm.drmDfsRead(path)(sc)
  }//loadFromHdfs
  
  def loadFromFile()={
    
  }//loadFromFile
  
  def saveToHdfs()={
    
  }//saveToHdfs
  
  def saveToFiles[T]()={
    
  }//saveToFiles
  
}