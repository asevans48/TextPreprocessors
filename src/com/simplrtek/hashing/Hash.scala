package com.simplrtek.hashing

import scala.util.hashing.MurmurHash3
import com.google.common.hash.Murmur3_32HashFunction

/**
 * Several hashing algorithms for such tasks as the hashing trick.
 */
object Hash {
  
  /**
   * Use Horners Hash to hash a string.
   * 
   * @param		text		The string to hash
   * @return	The signed integer from the hash.
   */
  def hornersHash(text:String,prime:Integer = 31):Long={
    var hash:Long = 0L
    for(i <- 0 to text.size){
      hash += (prime * hash) + text.charAt(i)
    }
    hash
  }
  
  /**
   * Use murmurHash3 to hash a byte array.
   * 
   * @param		data		The byte array to hash
   * @return	Returns the hashed integer.
   */
  def murmurHash(data:Array[Byte]):Integer={
    //Murmur3_32HashFunction
    MurmurHash3.bytesHash(data)  
  }
  
  /**
   * Use murmurHash3 to create a 32 bit based integer hash from a string and 
   * return it as a long.
   * 
   * @param		text		The string to hash
   * @return	The hash Integer as a long.
   */
  def murmurHashString(text:String):Integer={
    MurmurHash3.stringHash(text)
    
  }
  
}