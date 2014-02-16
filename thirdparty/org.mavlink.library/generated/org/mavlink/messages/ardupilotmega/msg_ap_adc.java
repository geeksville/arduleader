/**
 * Generated class : msg_ap_adc
 * DO NOT MODIFY!
 **/
package org.mavlink.messages.ardupilotmega;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.IMAVLinkCRC;
import org.mavlink.MAVLinkCRC;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Class msg_ap_adc
 * raw ADC output
 **/
public class msg_ap_adc extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_AP_ADC = 153;
  private static final long serialVersionUID = MAVLINK_MSG_ID_AP_ADC;
  public msg_ap_adc(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_AP_ADC;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 12;
}

  /**
   * ADC output 1
   */
  public int adc1;
  /**
   * ADC output 2
   */
  public int adc2;
  /**
   * ADC output 3
   */
  public int adc3;
  /**
   * ADC output 4
   */
  public int adc4;
  /**
   * ADC output 5
   */
  public int adc5;
  /**
   * ADC output 6
   */
  public int adc6;
/**
 * Decode message with raw data
 */
public void decode(ByteBuffer dis) throws IOException {
  adc1 = (int)dis.getShort()&0x00FFFF;
  adc2 = (int)dis.getShort()&0x00FFFF;
  adc3 = (int)dis.getShort()&0x00FFFF;
  adc4 = (int)dis.getShort()&0x00FFFF;
  adc5 = (int)dis.getShort()&0x00FFFF;
  adc6 = (int)dis.getShort()&0x00FFFF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+12];
   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
  dos.put((byte)0xFE);
  dos.put((byte)(length & 0x00FF));
  dos.put((byte)(sequence & 0x00FF));
  dos.put((byte)(sysId & 0x00FF));
  dos.put((byte)(componentId & 0x00FF));
  dos.put((byte)(messageType & 0x00FF));
  dos.putShort((short)(adc1&0x00FFFF));
  dos.putShort((short)(adc2&0x00FFFF));
  dos.putShort((short)(adc3&0x00FFFF));
  dos.putShort((short)(adc4&0x00FFFF));
  dos.putShort((short)(adc5&0x00FFFF));
  dos.putShort((short)(adc6&0x00FFFF));
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 12);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[18] = crcl;
  buffer[19] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_AP_ADC : " +   "  adc1="+adc1+  "  adc2="+adc2+  "  adc3="+adc3+  "  adc4="+adc4+  "  adc5="+adc5+  "  adc6="+adc6;}
}
