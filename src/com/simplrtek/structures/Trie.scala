package com.simplrtek.structures

/**
 * A try is basically a tree that minimizes memory use and can possibly be used to improve speed in situations where a list would
 * normally be traversed (but with some trade-offs +/- over a Map). This class implements a trie (digital tree/radix tree/prefix tree)
 * and allows the nodes to store anything. They must have pointer arrays to other nodes. A root node is provided to allow attachment 
 * when no other node is attached.
 * 
 * Another purpose of root is to make the tree searchable like a binary tree. The first node pointer should be in the binary-like structure. The
 * rest are valid nodes.
 * 
 * @author Andrew Evans 
 */
class Trie[K,V] {
  
  class Node extends MultiTreeNode[K,V] with Comparable[K]{
    var children: List[K] = List[K]()
    var root:K = _
    var data:V = _
    
    def addChild(node:K)={
      
    }//addChild
    
    def removeChild(node:K)={
      
    }//removeChild
    
     def compareTo(o:K):Int={
      
      -1
    }//compareTo   
  }
  
  def insertNode()={
    
  }
  
  def removeNode()={
    
  }
  
 
}