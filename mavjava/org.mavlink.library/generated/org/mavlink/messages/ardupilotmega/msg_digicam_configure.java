/**
 * Generated class : msg_digicam_configure
 * DO NOT MODIFY!
 **/
package org.mavlink.messages.ardupilotmega;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.IMAVLinkCRC;
import org.mavlink.MAVLinkCRC;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mavlink.io.LittleEndianDataInputStream;
import org.mavlink.io.LittleEndianDataOutputStream;
/**
 * Class msg_digicam_configure
 * Configure on-board Camera Control System.
 **/
public class msg_digicam_configure extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_DIGICAM_CONFIGURE = 154;
  private static final long serialVersionUID = MAVLINK_MSG_ID_DIGICAM_CONFIGURE;
  public msg_digicam_configure(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_DIGICAM_CONFIGURE;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 15;
}

  /**
   * Correspondent value to given extra_param
   */
  public float extra_value;
  /**
   * Divisor number //e.g. 1000 means 1/1000 (0 means ignore)
   */
  public int shutter_speed;
  /**
   * System ID
   */
  public int target_system;
  /**
   * Component ID
   */
  public int target_component;
  /**
   * Mode enumeration from 1 to N //P, TV, AV, M, Etc (0 means ignore)
   */
  public int mode;
  /**
   * F stop number x 10 //e.g. 28 means 2.8 (0 means ignore)
   */
  public int aperture;
  /**
   * ISO enumeration from 1 to N //e.g. 80, 100, 200, Etc (0 means ignore)
   */
  public int iso;
  /**
   * Exposure type enumeration from 1 to N (0 means ignore)
   */
  public int exposure_type;
  /**
   * Command Identity (incremental loop: 0 to 255)//A command sent multiple times will be executed or pooled just once
   */
  public int command_id;
  /**
   * Main engine cut-off time before camera trigger in seconds/10 (0 means no cut-off)
   */
  public int engine_cut_off;
  /**
   * Extra parameters enumeration (0 means ignore)
   */
  public int extra_param;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  extra_value = (float)dis.readFloat();
  shutter_speed = (int)dis.readUnsignedShort()&0x00FFFF;
  target_system = (int)dis.readUnsignedByte()&0x00FF;
  target_component = (int)dis.readUnsignedByte()&0x00FF;
  mode = (int)dis.readUnsignedByte()&0x00FF;
  aperture = (int)dis.readUnsignedByte()&0x00FF;
  iso = (int)dis.readUnsignedByte()&0x00FF;
  exposure_type = (int)dis.readUnsignedByte()&0x00FF;
  command_id = (int)dis.readUnsignedByte()&0x00FF;
  engine_cut_off = (int)dis.readUnsignedByte()&0x00FF;
  extra_param = (int)dis.readUnsignedByte()&0x00FF;
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+15];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(extra_value);
  dos.writeShort(shutter_speed&0x00FFFF);
  dos.writeByte(target_system&0x00FF);
  dos.writeByte(target_component&0x00FF);
  dos.writeByte(mode&0x00FF);
  dos.writeByte(aperture&0x00FF);
  dos.writeByte(iso&0x00FF);
  dos.writeByte(exposure_type&0x00FF);
  dos.writeByte(command_id&0x00FF);
  dos.writeByte(engine_cut_off&0x00FF);
  dos.writeByte(extra_param&0x00FF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 15);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[21] = crcl;
  buffer[22] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_DIGICAM_CONFIGURE : " +   "  extra_value="+extra_value+  "  shutter_speed="+shutter_speed+  "  target_system="+target_system+  "  target_component="+target_component+  "  mode="+mode+  "  aperture="+aperture+  "  iso="+iso+  "  exposure_type="+exposure_type+  "  command_id="+command_id+  "  engine_cut_off="+engine_cut_off+  "  extra_param="+extra_param;}
}
