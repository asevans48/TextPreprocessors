package com.simplrtek.pickle

import java.io.{ObjectInputStream,ObjectOutputStream}
import java.io.{File,FileOutputStream,FileInputStream}
import java.io.{IOException,FileNotFoundException}
import org.apache.commons.lang3.exception.ExceptionUtils

import com.simplrtek.logger.logger

/**
 * Pickles objects (which is the process) using Java Object Array Streams to maintain compatibility with
 * Scala 2.10.5+ versions of Scala 2.10.X. Paradise is missing the specific macros
 * for anything over Scala 2.10.4. Mahout Scala bindings require Scala 2.10.X.
 * 
 * The implementation is binary by default. A faster library will be used when Mahout bindings
 * are updated to 2.11.X + 
 *
 * Primitives and basic objects can be pickled as is but classes and objects require extending Serializable.
 * The annotation is deprecated. 
 */
object Pickler {
  
  protected var log = logger.getLogger()
  
  /**
   * Pickle an object to a file.
   * 
   * @param		picklee		The object of type [T] to pickle.
   * @param		file			The file to pickle to.
   */
  def pickleTo[T](picklee:T,file:File)={
    try{
      val foos = new FileOutputStream(file)
      val oos = new ObjectOutputStream(foos)
      oos.writeObject(picklee)
      oos.close()
      foos.close()
    }catch{
      case e:IOException =>{
        log.info("IO Exception in Writing Pickle: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
      }
      case e:NullPointerException =>{
        log.info("Picklee object or file are Null: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
      }
      case t:Throwable =>{
        log.info("Error in Pickling: "+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
      }
    }
  }
  
  /**
   * Unpickle a binary object from a file.
   * 
   * @param		file		The file to unpickle from.
   * @return	The object of type [T] from the file.
   */
  def unpickleFrom[T](file:File):T={
    try{
      val fois:FileInputStream = new FileInputStream(file)
      val ois:ObjectInputStream = new ObjectInputStream(fois)
      ois.readObject().asInstanceOf[T]
    }catch{
       case e:IOException =>{
        log.info("IO Exception in UnPickling from file: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        sys.exit()
      }
      case e:NullPointerException =>{
        log.info("Unpickle File is Null: "+e.getMessage+"\n"+ExceptionUtils.getStackTrace(e))
        sys.exit()
      }
      case t:Throwable =>{
        log.info("Error in UnPickling from File: "+t.getMessage+"\n"+ExceptionUtils.getStackTrace(t))
        sys.exit()
      }
      
    }
  }
  
}