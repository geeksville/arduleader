/**
 * $Id: MAVLinkReader.java 667 2012-11-16 13:30:24Z ghelle $
 * $Date: 2012-11-16 14:30:24 +0100 (ven., 16 nov. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLINK Java
 * Module : org.mavlink.library
 * File : org.mavlink.messages.MAVLinkReader.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	24 aout 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.messages.MAVLinkMessageFactory;

/**
 * @author ghelle
 * @version $Rev: 667 $
 * 
 */
public class MAVLinkReader {

  private boolean debug = true;

  /**
   * Input stream
   */
  private DataInputStream dis = null;

  /**
     * 
     */
  public final static int RECEIVED_BUFFER_SIZE = 512;

  /**
     * 
     */
  public final static int MAX_TM_SIZE = 263;

  /**
     * 
     */
  private final byte[] receivedBuffer = new byte[RECEIVED_BUFFER_SIZE];

  /**
   * Ne bytes received
   */
  private int nbReceived = 0;

  /**
   * Last sequence number received
   */
  private int[] lastSequence = new int[256];

  /**
   * Read MAVLink V1.0 packets by default;
   */
  private byte start = IMAVLinkMessage.MAVPROT_PACKET_START_V10;

  /**
   * MAVLink messages received
   */
  private Vector packets = new Vector();

  /**
   * True if we are reading a message
   */
  private boolean messageInProgress = false;

  /**
   * True if we have received the payload length
   */
  private boolean lengthReceived = false;

  private int lengthToRead = 0;

  private int lostBytes = 0;
  private int badSequence = 0;
  private int badCRC = 0;

  /**
   * Constructor with MAVLink 1.0 by default
   * 
   * @param dis
   *          Data input stream
   */
  public MAVLinkReader(DataInputStream dis) {
    this(dis, IMAVLinkMessage.MAVPROT_PACKET_START_V10);
  }

  /**
   * Constructor
   * 
   * @param dis
   *          Data input stream
   * @param start
   *          Start byte for MAVLink version
   */
  public MAVLinkReader(DataInputStream dis, byte start) {
    this.dis = dis;
    this.start = start;
    Arrays.fill(lastSequence, -1);
  }

  /**
   * Return next message. If bytes available, try to read it.
   * 
   * @return MAVLink message or null
   */
  public MAVLinkMessage getNextMessage() throws IOException {
    MAVLinkMessage msg = null;
    if (packets.isEmpty()) {
      readNextMessage();
    }
    if (!packets.isEmpty()) {
      msg = (MAVLinkMessage) packets.firstElement();
      packets.removeElementAt(0);
    }
    return msg;
  }

  /**
   * Return next message. If bytes available, try to read it. Don't wait message
   * is completed, it will be retruned nex time
   * 
   * @return MAVLink message or null
   */
  public MAVLinkMessage getNextMessageWithoutBlocking() {
    MAVLinkMessage msg = null;
    if (packets.isEmpty()) {
      readNextMessageWithoutBlocking();
    }
    if (!packets.isEmpty()) {
      msg = (MAVLinkMessage) packets.firstElement();
      packets.removeElementAt(0);
    }
    return msg;
  }

  /**
   * Try to read next message Can be blocked on read
   * 
   * @return true if data are valid
   */
  protected boolean readNextMessage() throws IOException {
    boolean validData = false;
    int length;
    int sequence;
    int sysId;
    int componentId;
    int msgId;
    byte crcLow;
    byte crcHigh;
    byte[] rawData = null;
    MAVLinkMessage msg = null;

    // we are allowed to block in this version of the function, take advantage
    // of that ASAP
    // otherwise getNextMessage will burn 100% of the CPU spinning...
    // if (dis.available() == 0)
    // return validData;
    receivedBuffer[nbReceived] = dis.readByte();
    if (receivedBuffer[nbReceived++] == start) {
      validData = true;

      length = receivedBuffer[nbReceived++] = dis.readByte();
      length &= 0X00FF;

      sequence = receivedBuffer[nbReceived++] = dis.readByte();
      sequence &= 0X00FF;

      sysId = receivedBuffer[nbReceived++] = dis.readByte();
      sysId &= 0X00FF;

      componentId = receivedBuffer[nbReceived++] = dis.readByte();
      componentId &= 0X00FF;

      msgId = receivedBuffer[nbReceived++] = dis.readByte();
      msgId &= 0X00FF;

      rawData = readRawData(length);

      crcLow = receivedBuffer[nbReceived++] = dis.readByte();
      crcHigh = receivedBuffer[nbReceived++] = dis.readByte();
      int crc = MAVLinkCRC.crc_calculate_decode(receivedBuffer, length);
      if (IMAVLinkCRC.MAVLINK_EXTRA_CRC) {
        // CRC-EXTRA for Mavlink 1.0
        crc = MAVLinkCRC.crc_accumulate(
            (byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[msgId], crc);
      }

      byte crcl = (byte) (crc & 0x00FF);
      byte crch = (byte) ((crc >> 8) & 0x00FF);
      if ((crcl == crcLow) && (crch == crcHigh)) {
        msg = MAVLinkMessageFactory.getMessage(msgId, sysId, componentId,
            rawData);
        if (msg != null) {
          msg.sequence = sequence;
          if (!checkSequence(sysId, sequence)) {
            badSequence += 1;
            /*
             * System.err.println("SEQUENCE error, packets lost! Last sequence : "
             * + lastSequence[sysId] + " Current sequence : " + sequence +
             * " Id=" + msgId + " nbReceived=" + nbReceived);
             */
          }
          packets.addElement(msg);
          // if (debug)
          // System.out.println("MESSAGE = " + msg);
        } else {
          System.err.println("ERROR creating message  Id=" + msgId);
          validData = false;
        }

        // Mark this sequence # as current
        lastSequence[sysId] = sequence;
      } else {
        badCRC += 1;
        // stdout very slow on android
        /*
         * System.err.println("ERROR mavlink CRC16-CCITT compute= " +
         * Integer.toHexString(crc) + "  expected : " +
         * Integer.toHexString(crcHigh & 0x00FF) + Integer.toHexString(crcLow &
         * 0x00FF) + " Id=" + msgId + " nbReceived=" + nbReceived);
         */
        validData = false;
      }
      // restart buffer
      nbReceived = 0;
    } else {
      validData = false;
      // Don't spam the log while syncing, client can get lostBytes if curious
      lostBytes++;
      // System.err.println("LOST bytes : " + lostBytes);
      // restart buffer
      nbReceived = 0;
    }

    return validData;
  }

  public int getLostBytes() {
    return lostBytes;
  }

  public int getBadSequence() {
    return badSequence;
  }

  public int getBadCRC() {
    return badCRC;
  }

  /**
   * Try to read next message Can be blocked on read
   * 
   * @return true if data are valid
   */
  protected boolean readNextMessageWithoutBlocking() {
    boolean validData = false;
    try {
      if (messageInProgress == false) {
        // Waiting for a new message
        if (dis.available() == 0)
          return validData;
        receivedBuffer[nbReceived] = dis.readByte();
        if (receivedBuffer[nbReceived++] == start) {
          messageInProgress = true;
          if (dis.available() == 0)
            return validData;
          lengthToRead = receivedBuffer[nbReceived++] = dis.readByte();
          lengthToRead &= 0X00FF;
          lengthReceived = true;

          if (dis.available() < 6 + lengthToRead)
            return validData;
          validData = readEndMessage();
          messageInProgress = false;
          lengthReceived = false;
          // restart buffer
          nbReceived = 0;
        } else {
          System.err.println("LOST bytes : " + lostBytes++);
          nbReceived = 0;
          return validData;
        }
      } else {
        // Message in progress
        if (!lengthReceived) {
          if (dis.available() == 0)
            return validData;
          lengthToRead = receivedBuffer[nbReceived++] = dis.readByte();
          lengthToRead &= 0X00FF;
          lengthReceived = true;
        }
        if (dis.available() < 6 + lengthToRead)
          return validData;
        validData = readEndMessage();
        messageInProgress = false;
        lengthReceived = false;
        // restart buffer
        nbReceived = 0;
      }
    } catch (java.io.EOFException eof) {
      System.err.println("ERROR EOF : " + eof);
      eof.printStackTrace();
      nbReceived = 0;
      validData = false;
    } catch (Exception e) {
      System.err.println("ERROR : " + e);
      // restart buffer
      nbReceived = 0;
      validData = false;
    }

    return validData;
  }

  /**
   * Read the end of message after the start byte and the payload length. Called
   * only if there are available character to read all the rest of message
   * 
   * @return true if no error occurs
   * @throws IOException
   *           on read byte function...
   */
  protected boolean readEndMessage() throws IOException {
    boolean validData = false;
    int sequence;
    int sysId;
    int componentId;
    int msgId;
    byte crcLow;
    byte crcHigh;
    byte[] rawData = null;
    MAVLinkMessage msg = null;
    sequence = receivedBuffer[nbReceived++] = dis.readByte();
    sequence &= 0X00FF;

    sysId = receivedBuffer[nbReceived++] = dis.readByte();
    sysId &= 0X00FF;

    componentId = receivedBuffer[nbReceived++] = dis.readByte();
    componentId &= 0X00FF;

    msgId = receivedBuffer[nbReceived++] = dis.readByte();
    msgId &= 0X00FF;

    rawData = readRawData(lengthToRead);

    crcLow = receivedBuffer[nbReceived++] = dis.readByte();
    crcHigh = receivedBuffer[nbReceived++] = dis.readByte();
    int crc = MAVLinkCRC.crc_calculate_decode(receivedBuffer, lengthToRead);
    if (IMAVLinkCRC.MAVLINK_EXTRA_CRC) {
      // CRC-EXTRA for Mavlink 1.0
      crc = MAVLinkCRC.crc_accumulate(
          (byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[msgId], crc);
    }

    byte crcl = (byte) (crc & 0x00FF);
    byte crch = (byte) ((crc >> 8) & 0x00FF);
    if ((crcl == crcLow) && (crch == crcHigh)) {
      msg = MAVLinkMessageFactory
          .getMessage(msgId, sysId, componentId, rawData);
      if (msg != null) {
        msg.sequence = sequence;
        if (!checkSequence(sysId, sequence)) {
          System.err.println("SEQUENCE error, packets lost! Last sequence : "
              + lastSequence[sysId] + " Current sequence : " + sequence
              + " Id=" + msgId + " nbReceived=" + nbReceived);
        }
        packets.addElement(msg);
        // if (debug)
        // System.out.println("MESSAGE = " + msg);
      } else {
        System.err.println("ERROR creating message  Id=" + msgId);
        validData = false;
      }
    } else {
      System.err.println("ERROR mavlink CRC16-CCITT compute= "
          + Integer.toHexString(crc) + "  expected : "
          + Integer.toHexString(crcHigh & 0x00FF)
          + Integer.toHexString(crcLow & 0x00FF) + " Id=" + msgId
          + " nbReceived=" + nbReceived);
      validData = false;
    }
    // restart buffer
    lastSequence[sysId] = sequence;
    nbReceived = 0;

    return validData;
  }

  /**
   * Check if we don't lost messages...
   * 
   * @param sequence
   *          current sequence
   * @return true if we don't lost messages
   */
  protected boolean checkSequence(int sysId, int sequence) {
    boolean check = false;
    if (lastSequence[sysId] == -1) {
      // it is the first message read
      lastSequence[sysId] = sequence;
      check = true;
    } else if (lastSequence[sysId] < sequence) {
      if (sequence - lastSequence[sysId] == 1) {
        // No message lost
        check = true;
      }
    } else
    // We have reached the max number (255) and restart to 0
    if (sequence + 256 - lastSequence[sysId] == 1) {
      // No message lost
      check = true;
    }
    return check;
  }

  /**
   * Read Payload bytes
   * 
   * @param nb
   *          Nb bytes to read
   * @return Payload bytes
   * @throws IOException
   */
  protected byte[] readRawData(int nb) throws IOException {
    byte[] buffer = new byte[nb];
    int index = 0;
    /*
     * while (dis.available() < nb) { ; }
     */
    for (int i = 0; i < nb; i++) {
      receivedBuffer[nbReceived] = dis.readByte();
      buffer[index++] = receivedBuffer[nbReceived++];
    }
    return buffer;
  }

}
