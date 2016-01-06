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
  
  var rootNode: Node = new Node()
  
  /**
   * Inner Node class.
   */
   class Node{
    var children: List[Node] = List[Node]()
    var root:Node = _
    var data:V = _
    
    
    def setRoot(n:Node)={
      root = n
    }
    
    def setData(d:V)={
      data = d
    }
    
    def addChild(node:Node)={
      this.children = this.children :+ node
    }
  }
  
  /**
   * Check if the trie contains a child.
   * 
   * @param			data					The data to check for.
   * @param			children			The children to check
   * @return		The node if contained or null if not.
   */
  def containsChild(data:V,children:List[Node]):Node={
    children.foreach { x =>  
        if(x.data.equals(data)){
          return x
        }
    }
    null
  }
  
  /**
   * Insert data into the try.
   * 
   * @param		data							The data to insert split into a list.
   * @param		{Node}{inN}				The starting node defaulting to root.
   * @param		{Integer}{start}	The datapoint to start from.
   */
  def insertData(data:List[V],inN:Node = rootNode,start:Integer = 0)={
     var n:Node = inN
     var tempN:Node = inN
     var i:Integer = start
     
     while(i < data.size){
       if(tempN != null){
          tempN=this.containsChild(data(i),n.children)
       }
       
       if(tempN == null){
         val n2 = new Node()
         n2.data = data(i)
         n2.root = rootNode
         n.children = n.children :+ n2
         n=n2
       }else{
         n = tempN
       }
       i += 1
     }
     
  }
  
  /**
   * Check if the data is contained in the trie and add if specified.
   * 
   * @param			data									The data to check for
   * @param			{Boolean}{insert}			Whether to add to the trie if the data is not present
   * @return		Whether or not the data is in the try.
   */
  def contains(data:List[V],insert:Boolean = false):Boolean={
    var n:Node = rootNode
    var tempN:Node = rootNode
    for(i <- 0 to data.size){
      if(tempN != null){
        tempN = this.containsChild(data(i), n.children)
      }
      
      if(tempN == null){
        if(insert){
          this.insertData(data, n, i)
        }
        return false
      }else{
        n = tempN
      }
    } 
    true
  }
}