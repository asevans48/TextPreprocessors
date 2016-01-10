package com.simplrtek.preprocessors

import scala.concurrent.{Future,Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import java.util.concurrent.ConcurrentHashMap

import com.simplrtek.vectorizers.WordCountVectorizer

import breeze.linalg.DenseMatrix
import breeze.linalg.DenseVector

import sbt.io.IO
import java.io.File

import com.simplrtek.pickle.Pickler


/**
 * Replace words in an object and create a replacement model. This is not distributable because of the iterative nature of building a replacement model.
 * However, concurrency is used where possible.
 */
class WordGraphReplacer(cols:Integer = 100, rows:Integer = 100){
  
  
}

object TestSDriver{
  def main(args:Array[String])={

  }
}