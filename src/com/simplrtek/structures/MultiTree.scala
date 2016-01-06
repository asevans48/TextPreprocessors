package com.simplrtek.structures

trait MultiTree[T,V]{
  var root:T
  def insertNode(n:T)
  def removeNode(n:T)
}

trait MultiTreeNode[T,V]{
  var children: List[T]
  var root:T
  var data:V
  
  def addChild(node:T)
  def removeChild(node:T)
}