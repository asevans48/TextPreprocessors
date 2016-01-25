package com.simplrtek.enriched.breeze

import breeze.linalg.SparseVector

class EnrichedSparseVector(vector : SparseVector[Double]){
  
  def getNNZ():Int={
    this.vector.activeSize
  }
  
  def ** (e : Double):SparseVector[Double]={
    this.vector.map { x => Math.pow(x, e) }
  }
  
}