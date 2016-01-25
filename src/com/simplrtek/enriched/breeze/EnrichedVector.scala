package com.simplrtek.enriched.breeze

import breeze.linalg.Vector

class EnrichedVector(vector : Vector[Double]) {
  def getNNZ():Int={
    vector.activeSize
  }
  
  
  def ** (n2 : Double):Vector[Double]={
    this.vector.map { x => Math.pow(x, n2)}
 
  }
}