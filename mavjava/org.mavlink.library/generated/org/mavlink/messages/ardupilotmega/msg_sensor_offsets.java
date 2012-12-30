/**
 * Generated class : msg_sensor_offsets
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
 * Class msg_sensor_offsets
 * Offsets and calibrations values for hardware
        sensors. This makes it easier to debug the calibration process.
 **/
public class msg_sensor_offsets extends MAVLinkMessage {
  public static final int MAVLINK_MSG_ID_SENSOR_OFFSETS = 150;
  private static final long serialVersionUID = MAVLINK_MSG_ID_SENSOR_OFFSETS;
  public msg_sensor_offsets(int sysId, int componentId) {
    messageType = MAVLINK_MSG_ID_SENSOR_OFFSETS;
    this.sysId = sysId;
    this.componentId = componentId;
    length = 42;
}

  /**
   * magnetic declination (radians)
   */
  public float mag_declination;
  /**
   * raw pressure from barometer
   */
  public long raw_press;
  /**
   * raw temperature from barometer
   */
  public long raw_temp;
  /**
   * gyro X calibration
   */
  public float gyro_cal_x;
  /**
   * gyro Y calibration
   */
  public float gyro_cal_y;
  /**
   * gyro Z calibration
   */
  public float gyro_cal_z;
  /**
   * accel X calibration
   */
  public float accel_cal_x;
  /**
   * accel Y calibration
   */
  public float accel_cal_y;
  /**
   * accel Z calibration
   */
  public float accel_cal_z;
  /**
   * magnetometer X offset
   */
  public int mag_ofs_x;
  /**
   * magnetometer Y offset
   */
  public int mag_ofs_y;
  /**
   * magnetometer Z offset
   */
  public int mag_ofs_z;
/**
 * Decode message with raw data
 */
public void decode(LittleEndianDataInputStream dis) throws IOException {
  mag_declination = (float)dis.readFloat();
  raw_press = (int)dis.readInt();
  raw_temp = (int)dis.readInt();
  gyro_cal_x = (float)dis.readFloat();
  gyro_cal_y = (float)dis.readFloat();
  gyro_cal_z = (float)dis.readFloat();
  accel_cal_x = (float)dis.readFloat();
  accel_cal_y = (float)dis.readFloat();
  accel_cal_z = (float)dis.readFloat();
  mag_ofs_x = (int)dis.readShort();
  mag_ofs_y = (int)dis.readShort();
  mag_ofs_z = (int)dis.readShort();
}
/**
 * Encode message with raw data and other informations
 */
public byte[] encode() throws IOException {
  byte[] buffer = new byte[8+42];
   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());
  dos.writeByte((byte)0xFE);
  dos.writeByte(length & 0x00FF);
  dos.writeByte(sequence & 0x00FF);
  dos.writeByte(sysId & 0x00FF);
  dos.writeByte(componentId & 0x00FF);
  dos.writeByte(messageType & 0x00FF);
  dos.writeFloat(mag_declination);
  dos.writeInt((int)(raw_press&0x00FFFFFFFF));
  dos.writeInt((int)(raw_temp&0x00FFFFFFFF));
  dos.writeFloat(gyro_cal_x);
  dos.writeFloat(gyro_cal_y);
  dos.writeFloat(gyro_cal_z);
  dos.writeFloat(accel_cal_x);
  dos.writeFloat(accel_cal_y);
  dos.writeFloat(accel_cal_z);
  dos.writeShort(mag_ofs_x&0x00FFFF);
  dos.writeShort(mag_ofs_y&0x00FFFF);
  dos.writeShort(mag_ofs_z&0x00FFFF);
  dos.flush();
  byte[] tmp = dos.toByteArray();
  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];
  int crc = MAVLinkCRC.crc_calculate_encode(buffer, 42);
  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);
  byte crcl = (byte) (crc & 0x00FF);
  byte crch = (byte) ((crc >> 8) & 0x00FF);
  buffer[48] = crcl;
  buffer[49] = crch;
  return buffer;
}
public String toString() {
return "MAVLINK_MSG_ID_SENSOR_OFFSETS : " +   "  mag_declination="+mag_declination+  "  raw_press="+raw_press+  "  raw_temp="+raw_temp+  "  gyro_cal_x="+gyro_cal_x+  "  gyro_cal_y="+gyro_cal_y+  "  gyro_cal_z="+gyro_cal_z+  "  accel_cal_x="+accel_cal_x+  "  accel_cal_y="+accel_cal_y+  "  accel_cal_z="+accel_cal_z+  "  mag_ofs_x="+mag_ofs_x+  "  mag_ofs_y="+mag_ofs_y+  "  mag_ofs_z="+mag_ofs_z;}
}
