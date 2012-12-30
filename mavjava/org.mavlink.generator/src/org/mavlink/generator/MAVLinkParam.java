/**
 * $Id: MAVLinkParam.java 662 2012-10-09 13:31:05Z ghelle $
 * $Date: 2012-10-09 15:31:05 +0200 (mar., 09 oct. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.MAVLinkParam.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	2 avr. 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.generator;

/**
 * MAVLink Param type
 * @author ghelle
 * @version $Rev: 662 $
 *
 */
public class MAVLinkParam {

    private int index;

    private String comment;

    public MAVLinkParam(int index) {
        this.index = index;
    }

    /**
     * @return The index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index The index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment The comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

}
