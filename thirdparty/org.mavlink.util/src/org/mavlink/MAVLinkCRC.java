/**
 * $Id: MAVLinkCRC.java 663 2012-10-09 13:31:35Z ghelle $
 * $Date: 2012-10-09 15:31:35 +0200 (mar., 09 oct. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLINK Java
 * Module : org.mavlink.library
 * File : org.mavlink.MAVLinkCRC.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	3 sept. 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink;

/**
 * MAVLink CRC computation
 * @author ghelle
 * @version $Rev: 663 $
 *
 */
public class MAVLinkCRC {

    /**
     * Convert a String in byte array
     * @param data
     * @return
     */
    public static byte[] stringToByte(String data) {
        byte[] buffer = new byte[data.length()];
        for (int i = 0; i < data.length(); i++) {
            buffer[i] = (byte) data.charAt(i);
        }
        return buffer;
    }

    /**
     * Accumulate the X.25 CRC by adding one char at a time.
     * The checksum function adds the hash of one char at a time to the 16 bit checksum
     * @param data new char to hash
     * @param crc the already accumulated checksum
     * @return the new accumulated checksum
     */
    public static int crc_accumulate(byte data, int crc) {
        int tmp, tmpdata;
        int crcaccum = crc & 0x000000ff;
        tmpdata = data & 0x000000ff;
        tmp = tmpdata ^ crcaccum;
        tmp &= 0x000000ff;
        int tmp4 = tmp << 4;
        tmp4 &= 0x000000ff;
        tmp ^= tmp4;
        tmp &= 0x000000ff;
        int crch = crc >> 8;
        crch &= 0x0000ffff;
        int tmp8 = tmp << 8;
        tmp8 &= 0x0000ffff;
        int tmp3 = tmp << 3;
        tmp3 &= 0x0000ffff;
        tmp4 = tmp >> 4;
        tmp4 &= 0x0000ffff;
        int tmpa = crch ^ tmp8;
        tmpa &= 0x0000ffff;
        int tmpb = tmp3 ^ tmp4;
        tmpb &= 0x0000ffff;
        crc = tmpa ^ tmpb;
        crc &= 0x0000ffff;
        return crc;
        /*
        byte ch = (byte) (data ^ (byte) (crc & 0x00ff));
        ch = (byte) (ch ^ (ch << 4));
        return ((crc >> 8) ^ (ch << 8) ^ (ch << 3) ^ (ch >> 4));
        */
    }

    /**
     * Initialize the buffer for the X.25 CRC
     * @return the 16 bit X.25 CRC
     */
    public static int crc_init() {
        return IMAVLinkMessage.X25_INIT_CRC;
    }

    /**
     * Calculates the X.25 checksum to decode a MAVLink message stored in a byte buffer and begins after the MAVLink Packet start
     * @param buffer buffer containing the byte array to hash
     * @param dataLength length of the payload so the buffer size is 1 + payload + 5
     * @return the accumulated checksum
     */
    public static int crc_calculate_decode(byte[] buffer, int dataLength) {
        int crc = crc_init();
        for (int i = 1; i <= dataLength + IMAVLinkMessage.CRC_LEN; i++) {
            crc = crc_accumulate(buffer[i], crc);
        }
        return crc;
    }

    /**
     * Calculates the X.25 checksum to encode a MAVLink message stored in a byte buffer and begins after the MAVLink Packet start
     * @param buffer buffer containing the byte array to hash
     * @param dataLength total length of the MAVLink message so CRC is calculate between 1 and length - 2
     * @return the accumulated checksum
     */
    public static int crc_calculate_encode(byte[] buffer, int length) {
        int crc = crc_init();
        for (int i = 1; i < buffer.length - 2; i++) {
            crc = crc_accumulate(buffer[i], crc);
        }
        return crc;
    }

    /**
     * Calculates the X.25 checksum on a byte buffer
     * @param buffer buffer containing the byte array to hash
     * @return the accumulated checksum
     */
    public static int crc_calculate(byte[] buffer) {
        int crc = crc_init();
        for (int i = 0; i < buffer.length; i++) {
            crc = crc_accumulate(buffer[i], crc);
        }
        return crc;
    }

}
