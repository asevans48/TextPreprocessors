package com.simplrtek.preprocessors

object NumericConverter {
  def replace(text:String)={
    text.replaceAll("[0-9]+","#")
  }
}