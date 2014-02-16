/**
 * $Id: FieldCompare.java 662 2012-10-09 13:31:05Z ghelle $
 * $Date: 2012-10-09 15:31:05 +0200 (mar., 09 oct. 2012) $
 *
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLINK Java
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.FieldCompare.java
 * Author : Guillaume Helle
 *
 * ======================================================
 * HISTORY
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle	7 sept. 2012		Create
 * 
 * ====================================================================
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.generator;

import java.util.Comparator;

/**
 * Comparator to sort field in MAVLink messages.
 * Sort only on the size of field type and ignore array size
 * @author ghelle
 * @version $Rev: 662 $
 *
 */
public class FieldCompare implements Comparator<MAVLinkField> {

    /**
     * {@inheritDoc}
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(MAVLinkField field2, MAVLinkField field1) {
        //Sort on type size
        if (field1.getType().getTypeSize() > field2.getType().getTypeSize()) {
            return 1;
        }
        else if (field1.getType().getTypeSize() < field2.getType().getTypeSize()) {
            return -1;
        }
        return 0;
    }

}
