package com.simplrtek.preprocessors

import sbt.io.IO
import java.io.File

object TabToCSV {

  def convert(path:String,outpath:String,allColumns:Boolean = true, numCols:Int = 0)={
    var content:List[String] = null
    if(allColumns){
      content=IO.readLines(new File(path)).map{x => x.split("\t").mkString(",")}
    }else{
      content=IO.readLines(new File(path)).map{ x => x.split("\t").slice(0,numCols).mkString(",")}
    }
    IO.write(new File(outpath),content.mkString("\n"))
  }
}


object CSVDriver{
  
  def main(args:Array[String])={
    TabToCSV.convert("C:\\Users/packe/Documents/workspace-sts-3.7.1.RELEASE/MahoutTest/data/u.data","C:\\Users/packe/Documents/workspace-sts-3.7.1.RELEASE/MahoutTest/data/movies.csv", allColumns=false, numCols=3)
  }
  
}