package com.simplrtek.structures

/**
 * A try is basically a tree that minimizes memory use and can possibly be used to improve speed in situations where a list would
 * normally be traversed (but with some trade-offs +/- over a Map). This class implements a trie (digital tree/radix tree/prefix tree)
 * and allows the nodes to store anything. They must have pointer arrays to other nodes. A root node is provided to allow attachment 
 * when no other node is attached.
 * 
 * 
 * @author Andrew Evans 
 */
class Trie[V] {
  
  var root: Node = _
  
  class Node extends Comparable[Node]{
    var children: List[Node] = List[Node]()
    var root:Node = _
    var data:V = _
    
    def addChild(node:Node)={
      
    }//addChild
    
    override def compareTo(o:Node):Int={
      -1
    }//compareTo   
  }
  
  def insertData(data:List[V])={
    
  }//insertNode
  
  
  def contains(data:List[V]):Boolean={
    false
  }//search
}