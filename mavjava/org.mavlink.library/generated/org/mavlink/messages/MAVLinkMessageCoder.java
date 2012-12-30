/**
 * Generated class : MAVLinkMessageCoder
 * DO NOT MODIFY!
 **/
package org.mavlink.messages;
import java.io.IOException;
import org.mavlink.io.LittleEndianDataInputStream;
/**
 * Class MAVLinkMessageCoder
 * Use to declarate encode and decode functions
 **/
public abstract class MAVLinkMessageCoder {
  /**
   * Decode message with raw data
   */
  public abstract void decode(LittleEndianDataInputStream dis) throws IOException ;
  /**
   * Encode message in raw data
   */
  public abstract byte[] encode() throws IOException ;
}
