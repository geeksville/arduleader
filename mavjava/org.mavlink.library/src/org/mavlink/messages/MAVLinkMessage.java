/**
 * $Id: MAVLinkMessage.java 667 2012-11-16 13:30:24Z ghelle $
 * $Date: 2012-11-16 14:30:24 +0100 (ven., 16 nov. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.library
 * File : org.mavlink.messages.MAVLinkMessage.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	5 avr. 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.messages;

import java.io.IOException;

import org.mavlink.IMAVLinkMessage;

/**
 * Common interface for all MAVLink Messages
 * Packet Anatomy
 * This is the anatomy of one packet. It is inspired by the CAN and SAE AS-4 standards. 

 * Byte Index  Content              Value       Explanation  
 * 0            Packet start sign  v1.0: 0xFE   Indicates the start of a new packet.  (v0.9: 0x55) 
 * 1            Payload length      0 - 255     Indicates length of the following payload.  
 * 2            Packet sequence     0 - 255     Each component counts up his send sequence. Allows to detect packet loss  
 * 3            System ID           1 - 255     ID of the SENDING system. Allows to differentiate different MAVs on the same network.  
 * 4            Component ID        0 - 255     ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.  
 * 5            Message ID          0 - 255     ID of the message - the id defines what the payload means and how it should be correctly decoded.  
 * 6 to (n+6)   Data                0 - 255     Data of the message, depends on the message id.  
 * (n+7)to(n+8) Checksum (low byte, high byte)  ITU X.25/SAE AS-4 hash, excluding packet start sign, so bytes 1..(n+6) Note: The checksum also includes MAVLINK_CRC_EXTRA (Number computed from message fields. Protects the packet from decoding a different version of the same packet but with different variables).  

 * The checksum is the same as used in ITU X.25 and SAE AS-4 standards (CRC-16-CCITT), documented in SAE AS5669A. Please see the MAVLink source code for a documented C-implementation of it. LINK TO CHECKSUM
 * The minimum packet length is 8 bytes for acknowledgement packets without payload
 * The maximum packet length is 263 bytes for full payload
 * 
 * @author ghelle
 * @version $Rev: 667 $
 *
 */
public abstract class MAVLinkMessage extends MAVLinkMessageCoder implements IMAVLinkMessage {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 2000873536402943462L;

    /**
     * Message length
     */
    public int length = -1;

    /**
     * Message sequence
     */
    public int sequence = -1;

    /**
     * Indicates the type of message : -1 for AC1 Protocol, otherwise its a MAVLink packet
     */
    public int messageType = -1;

    /**
     * ID of the SENDING system. Allows to differentiate different MAVs on the same network.
     */
    public int sysId = -1;

    /**
     * ID of the SENDING component. Allows to differentiate different components of the same system, e.g. the IMU and the autopilot.
     */
    public int componentId = -1;

    /**
     * Raw data of TM for replay and storage
     */
    protected byte[] rawData = null;

    /**
     * True if message is correct
     */
    public boolean isValid = false;

    public MAVLinkMessage() {

    }

    /**
     * Constructor from raw data
     * 
     * @param raw data received
     */
    public MAVLinkMessage(byte[] raw) {
        rawData = raw;
    }

    /**
     * Decode TM with raw data
     */
    public boolean check() throws IOException {
        return false;
    }

}
