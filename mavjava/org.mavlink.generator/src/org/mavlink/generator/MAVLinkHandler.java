/**
 * $Id: MAVLinkHandler.java 667 2012-11-16 13:30:24Z ghelle $
 * $Date: 2012-11-16 14:30:24 +0100 (ven., 16 nov. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.MAVLinkHandler.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle   31 mars 2012        Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.generator;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for MAVLink xml files
 * @author ghelle
 * @version $Rev: 667 $
 */
public class MAVLinkHandler extends DefaultHandler implements IMAVLinkTag {

    private MAVLinkData mavlink;

    private boolean inMavlink;

    private boolean inInclude;

    private boolean inMessages;

    private boolean inMessage;

    private boolean inEntry;

    private boolean inEnums;

    private boolean inEnum;

    private boolean inParam;

    private boolean inField;

    private boolean inVersion;

    private boolean inDescription;

    private StringBuffer buffer;

    private MAVLinkEnum currentEnum;

    private MAVLinkEntry currentEntry;

    private MAVLinkField currentField;

    private MAVLinkMessage currentMessage;

    private MAVLinkParam currentParam;

    String path;

    String target;

    MAVLinkGenerator generator;

    public MAVLinkHandler(MAVLinkGenerator generator, MAVLinkData mavlink, String path, String target) {
        super();
        this.generator = generator;
        this.mavlink = mavlink;
        this.path = path;
        this.target = target;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase(INCLUDE_TAG)) {
            inInclude = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(MAVLINK_TAG)) {
            inMavlink = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(ENUMS_TAG)) {
            inEnums = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(MESSAGES_TAG)) {
            inMessages = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(ENUM_TAG)) {
            inEnum = true;
            buffer = new StringBuffer();
            try {
                String name = attributes.getValue(NAME_ATTR);
                currentEnum = mavlink.getEnums().get(name);
                if (currentEnum == null) {
                    currentEnum = new MAVLinkEnum(name);
                    mavlink.getEnums().put(name, currentEnum);
                }
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
        else if (qName.equalsIgnoreCase(MESSAGE_TAG)) {
            inMessage = true;
            buffer = new StringBuffer();
            try {
                String name = attributes.getValue(NAME_ATTR);
                String s = attributes.getValue(ID_ATTR);
                int id = s == null ? -1 : Integer.parseInt(attributes.getValue(ID_ATTR));
                currentMessage = mavlink.getMessages().get(name);
                if (currentMessage == null) {
                    currentMessage = new MAVLinkMessage(id, name);
                    mavlink.getMessages().put(name, currentMessage);
                }
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
        else if (qName.equalsIgnoreCase(ENTRY_TAG)) {
            inEntry = true;
            buffer = new StringBuffer();
            try {
                String name = attributes.getValue(NAME_ATTR);
                String s = attributes.getValue(VALUE_ATTR);
                int value = s == null ? -1 : Integer.parseInt(attributes.getValue(VALUE_ATTR));
                currentEntry = new MAVLinkEntry(value, name);
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
        else if (qName.equalsIgnoreCase(FIELD_TAG)) {
            inField = true;
            buffer = new StringBuffer();
            try {
                String name = attributes.getValue(NAME_ATTR);
                String type = attributes.getValue(TYPE_ATTR);
                currentField = new MAVLinkField(new MAVLinkDataType(type), name);
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
        else if (qName.equalsIgnoreCase(VERSION_TAG)) {
            inVersion = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(DESCRIPTION_TAG)) {
            inDescription = true;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(PARAM_TAG)) {
            inParam = true;
            buffer = new StringBuffer();
            try {
                String s = attributes.getValue(INDEX_ATTR);
                int index = s == null ? -1 : Integer.parseInt(attributes.getValue(INDEX_ATTR));
                currentParam = new MAVLinkParam(index);
            }
            catch (Exception e) {
                throw new SAXException(e);
            }
        }
        else {
            System.out.println("Unknown TAG : " + qName);
        }

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase(INCLUDE_TAG)) {
            inInclude = false;
            try {
                generator.parseFile(mavlink, buffer.toString().trim(), path, target, true);
            }
            catch (ParserConfigurationException e) {
                System.err.println("ERROR parsing file " + buffer.toString().trim() + " from " + path + " to " + target + " : " + e);
            }
            catch (IOException e) {
                System.err.println("ERROR parsing file " + buffer.toString().trim() + " from " + path + " to " + target + " : " + e);
            }
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(MAVLINK_TAG)) {
            inMavlink = false;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(ENUMS_TAG)) {
            inEnums = false;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(MESSAGES_TAG)) {
            inMessages = false;
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(ENUM_TAG)) {
            inEnum = false;
            //mavlink.getEnums().put(currentEnum.getName(),currentEnum);
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(MESSAGE_TAG)) {
            inMessage = false;
            //mavlink.getMessages().put(currentMessage.getName(),currentMessage);
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(ENTRY_TAG)) {
            inEntry = false;
            currentEnum.getEntries().add(currentEntry);
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(FIELD_TAG)) {
            inField = false;
            currentField.setDescription(buffer.toString());
            buffer = new StringBuffer();
            currentMessage.getFields().add(currentField);
        }
        else if (qName.equalsIgnoreCase(VERSION_TAG)) {
            inVersion = false;
            mavlink.setVersion(buffer.toString());
            buffer = new StringBuffer();
        }
        else if (qName.equalsIgnoreCase(DESCRIPTION_TAG)) {
            inDescription = false;
            if (inEntry) {
                currentEntry.setDescription(buffer.toString().trim());
                buffer = new StringBuffer();
            }
            else if (inEnum) {
                currentEnum.setDescription(buffer.toString().trim());
                buffer = new StringBuffer();
            }
            else if (inMessage) {
                currentMessage.setDescription(buffer.toString().trim());
                buffer = new StringBuffer();
            }
        }
        else if (qName.equalsIgnoreCase(PARAM_TAG)) {
            inParam = false;
            currentParam.setComment(buffer.toString());
            buffer = new StringBuffer();
            currentEntry.getParams().add(currentParam);
        }

    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String lecture = new String(ch, start, length);
        if (buffer == null) {
            buffer = new StringBuffer();
        }
        buffer.append(lecture);
    }

    // Begin parsing
    public void startDocument() throws SAXException {
    }

    // End parsing
    public void endDocument() throws SAXException {
    }

    /**
     * @return The mavlink
     */
    public MAVLinkData getMavlink() {
        return mavlink;
    }

}
