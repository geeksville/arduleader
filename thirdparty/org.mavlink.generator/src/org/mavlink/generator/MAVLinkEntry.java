/**
 * $Id: MAVLinkEntry.java 662 2012-10-09 13:31:05Z ghelle $
 * $Date: 2012-10-09 15:31:05 +0200 (mar., 09 oct. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.MAVLinkEntry.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * MAVLink Entry data
 * @author ghelle
 * @version $Rev: 662 $
 *
 */
public class MAVLinkEntry {

    /**
     * MAVLink Entry value
     */
    private int value;

    /**
     * MAVLink Entry name
     */
    private String name;

    /**
     * MAVLink Entry description
     */
    private String description;

    /**
     * MAVLink Entry params
     */
    private List<MAVLinkParam> params;

    /**
     * MAVLink Entry constructor
     * @param value
     * @param name
     */
    public MAVLinkEntry(int value, String name) {
        this.value = value;
        this.name = name;
        params = new ArrayList<MAVLinkParam>();
    }

    /**
     * @return The value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value The value to set
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The params
     */
    public List<MAVLinkParam> getParams() {
        return params;
    }

    /**
     * @param params The params to set
     */
    public void setParams(List<MAVLinkParam> params) {
        this.params = params;
    }

}
