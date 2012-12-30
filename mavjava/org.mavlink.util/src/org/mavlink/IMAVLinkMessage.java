/**
 * $Id: IMAVLinkMessage.java 667 2012-11-16 13:30:24Z ghelle $
 * $Date: 2012-11-16 14:30:24 +0100 (ven., 16 nov. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLINK Java
 * Module : org.mavlink.library
 * File : org.mavlink.messages.IMAVLinkMessage.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	13 aout 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink;

/**
 * Some constants for MAVLink
 * @author ghelle
 * @version $Rev: 667 $
 *
 */
public interface IMAVLinkMessage {

    /**
     * Packet start in MAVLink V1.0
     */
    public final static byte MAVPROT_PACKET_START_V10 = (byte) 0xFE;

    /**
     * Packet start in MAVLink V0.9
     */
    public final static byte MAVPROT_PACKET_START_V09 = (byte) 0x55;

    /**
     * Len to add to payload for CRC computing
     */
    public final static int CRC_LEN = 5;

    /**
     * Use to initialize CRC before computing
     */
    public final static int X25_INIT_CRC = 0x0000ffff;

    /**
     * Use to validate CRC
     */
    public final static int X25_VALIDATE_CRC = 0x0000f0b8;

}
