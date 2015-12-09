package com.simplrtek.preprocessors

import org.slf4j.{Logger,LoggerFactory}

/**
 * A logger standalone or companion object for the Slf4JLogger class.
 */
object logger{
  private var logger:Logger = LoggerFactory.getLogger(classOf[Slf4JLogger]) //logger
  
  /**
   *Log a message.
   * 
   * @param		logLevel		The logging level with a default of info ("warn","debug","error")
   */
  def log(logLevel:String,msg:String)={
    logLevel match{
      case "warn" => logger.warn(msg)
      case "debug" => logger.debug(msg)
      case "error" => logger.error(msg)
      case _ => logger.info(msg)
    }
  }
  
  /**
   * Return the logger. 
   */
  def getLogger():Logger={
    return logger
  }
}

/**
 * The logger class.
 */
class Slf4JLogger {
  
  /**
   * Log a message with the given logging level.
   * 
   *@param		logLevel		The logging level with a default of info ("warn","debug","error")
   *@see logger#log
   */
  def log(logLevel:String,msg:String)={
    logger.log(logLevel, msg)
  }
  
  /**
   * Return the logger from the companion class
   * @return	The logger.
   * @see logger#getLogger
   */
  def getLogger():Logger={
    return logger.getLogger()
  }
}