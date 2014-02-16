/**
 * Generated class : MAVLinkMessageCoder
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
import java.io.IOException;
import java.io.Serializable;import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Class MAVLinkMessageCoder
 * Use to declarate encode and decode functions
 **/
public abstract class MAVLinkMessageCoder  implements Serializable{
  /**
   * Decode message with raw data
   */
  public abstract void decode(ByteBuffer dis) throws IOException ;
  /**
   * Encode message in raw data
   */
  public abstract byte[] encode() throws IOException ;
}
