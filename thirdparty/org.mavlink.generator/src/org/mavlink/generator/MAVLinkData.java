/**
 * $Id: MAVLinkData.java 662 2012-10-09 13:31:05Z ghelle $
 * $Date: 2012-10-09 15:31:05 +0200 (mar., 09 oct. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.MAVLinkData.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	30 mars 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.generator;

import java.util.HashMap;
import java.util.Map;

/**
 * MAVLink data in a xml file : enums and messages
 * @author ghelle
 * @version $Rev: 662 $
 *
 */
public class MAVLinkData {

    private String version;

    private Map<String, MAVLinkEnum> enums;

    private Map<String, MAVLinkMessage> messages;

    private String file;

    public MAVLinkData() {
        enums = new HashMap<String, MAVLinkEnum>();
        messages = new HashMap<String, MAVLinkMessage>();
    }

    /**
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version The version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The enums
     */
    public Map<String, MAVLinkEnum> getEnums() {
        return enums;
    }

    /**
     * @param enums The enums to set
     */
    public void setEnums(Map<String, MAVLinkEnum> enums) {
        this.enums = enums;
    }

    /**
     * @return The messages
     */
    public Map<String, MAVLinkMessage> getMessages() {
        return messages;
    }

    /**
     * @param messages The messages to set
     */
    public void setMessages(Map<String, MAVLinkMessage> messages) {
        this.messages = messages;
    }

    /**
     * @return The file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file The file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

}
