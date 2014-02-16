/**
 * $Id: MAVLinkDataType.java 662 2012-10-09 13:31:05Z ghelle $ 
 * $Date: 2012-10-09 15:31:05 +0200 (mar., 09 oct. 2012) $ 
 * 
 * ====================================================== 
 * Copyright (C) 2012 Guillaume Helle. 
 * Project : MAVLink Java Generator 
 * Module : org.mavlink.generator 
 * File : org.mavlink.generator.MAVLinkDataType.java 
 * Author : Guillaume Helle
 * 
 * ====================================================== 
 * HISTORY 
 * Who       yyyy/mm/dd   Action
 * --------  ----------   ------
 * ghelle   30 mars 2012        Create
 * 
 * ==================================================================== 
 * Licence: MAVLink LGPL
 * ====================================================================
 */

package org.mavlink.generator;

/**
 * MAVLink data types and helpers
 * @author Capgemini
 * @version $Rev: 662 $
 */
public class MAVLinkDataType {

    /**
     * UNKNOWN type
     */
    public final static int UNKNOWN = -1;

    /**
     * CHAR type
     */
    public final static int CHAR = 0;

    /**
     * UINT8 type
     */
    public final static int UINT8 = 1;

    /**
     * INT8 type
     */
    public final static int INT8 = 2;

    /**
     * UINT16 type
     */
    public final static int UINT16 = 3;

    /**
     * INT16 type
     */
    public final static int INT16 = 4;

    /**
     * UINT32 type
     */
    public final static int UINT32 = 5;

    /**
     * INT32 type
     */
    public final static int INT32 = 6;

    /**
     * INT64 type
     */
    public final static int INT64 = 7;

    /**
     * UINT64 type
     */
    public final static int UINT64 = 8;

    /**
     * Float type
     */
    public final static int FLOAT = 9;

    /**
     * Double type
     */
    public final static int DOUBLE = 10;

    /**
     * Double type
     */
    public final static int ARRAY = 11;

    /**
     * MAVLink Data type
     */
    int type;

    /**
     * True if the type is an array
     */
    boolean isArray = false;

    /**
     * array length
     */
    int arrayLenth = -1;

    /**
     * Constructor with the C type name
     * @param nameC
     */
    public MAVLinkDataType(String nameC) {
        type = getType(nameC);
        if (nameC.indexOf('[') != -1) {
            isArray = true;
            String sLen = nameC.substring(nameC.indexOf('[') + 1, nameC.indexOf(']'));
            arrayLenth = Integer.parseInt(sLen);
        }
    }

    /**
     * Return C type name for enum type
     * @param type
     * @return C type name for enum type
     */
    public static String getCType(int type) {
        String value = null;
        switch (type) {
            case ARRAY:
                value = "array";
                break;
            case CHAR:
                value = "char";
                break;
            case UINT8:
                value = "uint8_t";
                break;
            case INT8:
                value = "int8_t";
                break;
            case INT16:
                value = "int16_t";
                break;
            case UINT16:
                value = "uint16_t";
                break;
            case INT32:
                value = "int32_t";
                break;
            case UINT32:
                value = "uint32_t";
                break;
            case INT64:
                value = "int64_t";
                break;
            case UINT64:
                value = "uint64_t";
                break;
            case DOUBLE:
                value = "double";
                break;
            case FLOAT:
                value = "float";
                break;
            default:
        }
        return value;
    }

    /**
     * Return Java type from enum type
     * @param type
     * @return Java type
     */
    public String getJavaTypeName(int type) {
        String value = null;
        switch (type) {
            case ARRAY:
            case CHAR:
                value = "char";
                break;
            case UINT8:
                value = "int";
                break;
            case INT8:
                value = "int";
                break;
            case INT16:
                value = "int";
                break;
            case UINT16:
                value = "int";
                break;
            case INT32:
                value = "long";
                break;
            case UINT32:
                value = "long";
                break;
            case UINT64:
                value = "long";
                break;
            case INT64:
                value = "long";
                break;
            case FLOAT:
                value = "float";
                break;
            case DOUBLE:
                value = "double";
                break;
            default:
        }
        return value;
    }

    /**
     * Return total type size : size type * array size
     * @return total type size
     */
    public int getLengthType() {
        int value = 0;
        switch (type) {
            case ARRAY:
            case CHAR:
                value = (isArray ? arrayLenth * 1 : 1);
                break;
            case UINT8:
                value = (isArray ? arrayLenth * 1 : 1);
                break;
            case INT8:
                value = (isArray ? arrayLenth * 1 : 1);
                break;
            case INT16:
                value = (isArray ? arrayLenth * 2 : 2);
                break;
            case UINT16:
                value = (isArray ? arrayLenth * 2 : 2);
                break;
            case INT32:
                value = (isArray ? arrayLenth * 4 : 4);
                break;
            case UINT32:
                value = (isArray ? arrayLenth * 4 : 4);
                break;
            case UINT64:
                value = (isArray ? arrayLenth * 8 : 8);
                break;
            case INT64:
                value = (isArray ? arrayLenth * 8 : 8);
                break;
            case FLOAT:
                value = (isArray ? arrayLenth * 4 : 4);
                break;
            case DOUBLE:
                value = (isArray ? arrayLenth * 8 : 8);
                break;
            default:
        }
        return value;
    }

    /**
     * return type size
     * @return type size
     */
    public int getTypeSize() {
        int value = 0;
        switch (type) {
            case ARRAY:
            case CHAR:
                value = 1;
                break;
            case UINT8:
                value = 1;
                break;
            case INT8:
                value = 1;
                break;
            case INT16:
                value = 2;
                break;
            case UINT16:
                value = 2;
                break;
            case INT32:
                value = 4;
                break;
            case UINT32:
                value = 4;
                break;
            case UINT64:
                value = 8;
                break;
            case INT64:
                value = 8;
                break;
            case FLOAT:
                value = 4;
                break;
            case DOUBLE:
                value = 8;
                break;
            default:
        }
        return value;
    }

    /**
     * Build Java type declaration in a String
     * @param type
     * @param isArray
     * @param length
     * @param name
     * @return Java type declaration
     */
    public String getJavaType(int type, boolean isArray, int length, String name) {
        String value = null;
        if (type == UNKNOWN) {
            System.out.println("getJavaType : " + type);
        }
        else {
            value = getJavaTypeName(type);
            if (isArray) {
                value = value + "[] " + name + " = new " + value + "[" + length + "]";
            }
            else {
                value = value + " " + name;
            }
            value = value + ";";
        }
        return value;
    }

    /**
     * Build Java type declaration in a String
     * @param name
     * @return Java type declaration
     */
    public String getJavaType(String name) {
        return getJavaType(type, isArray, arrayLenth, name);
    }

    /**
     * Return enum type from its name
     * @param name
     * @return enum type
     */
    public int getType(String name) {
        if (name.indexOf(MAVLinkDataType.getCType(UINT8)) != -1) {
            return UINT8;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(INT8)) != -1) {
            return INT8;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(UINT16)) != -1) {
            return UINT16;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(INT16)) != -1) {
            return INT16;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(UINT32)) != -1) {
            return UINT32;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(INT32)) != -1) {
            return INT32;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(CHAR)) != -1) {
            return CHAR;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(ARRAY)) != -1) {
            return ARRAY;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(FLOAT)) != -1) {
            return FLOAT;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(DOUBLE)) != -1) {
            return DOUBLE;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(UINT64)) != -1) {
            return UINT64;
        }
        else if (name.indexOf(MAVLinkDataType.getCType(INT64)) != -1) {
            return INT64;
        }
        System.out.println("Unknown Data Type : " + name);
        return UNKNOWN;
    }

    /**
     * return C Type
     * @return
     */
    public String getCType() {
        return getCType(type);
    }

    /**
     * Return code in a String to read the field in byte buffer.
     * Use different Java API if code is embedded
     * @param type name
     * @param forEmbeddedJava true if code must be embedded
     * @return the code reader for the field in a String
     */
    public String getReadType(String name, boolean forEmbeddedJava) {
        String value = "";
        String arrayString = "";
        String beginLoop = "";
        String endLoop = "  }\n";
        int length = arrayLenth;
        if (!isArray) {
            length = 1;
            arrayString = "";
            endLoop = "";
        }
        else {
            arrayString = "[i]";
            beginLoop = "  for (int i=0; i<" + length + "; i++) {\n";
            value = beginLoop;
        }

        value = value + "  " + (isArray ? "  " : "") + name + arrayString;
        switch (type) {
            case ARRAY:
            case CHAR:
                if (forEmbeddedJava) {
                    value = value + " = (char)dis.readByte();"; //"char"
                }
                else {
                    value = value + " = (char)dis.get();"; //"char";
                }
                break;
            case UINT8:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readUnsignedByte()&0x00FF;"; //"uint8_t";
                }
                else {
                    value = value + " = (int)dis.get()&0x00FF;"; //"uint8_t";
                }
                break;
            case INT8:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readByte();"; //"int8_t";
                }
                else {
                    value = value + " = (int)dis.get();"; //"int8_t";
                }
                break;
            case INT16:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readShort();"; //"int16_t";
                }
                else {
                    value = value + " = (int)dis.getShort();"; //"int16_t";
                }
                break;
            case UINT16:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readUnsignedShort()&0x00FFFF;"; //"uint16_t";
                }
                else {
                    value = value + " = (int)dis.getShort()&0x00FFFF;"; //"uint16_t";
                }
                break;
            case INT32:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readInt();"; //"int32_t";
                }
                else {
                    value = value + " = (int)dis.getInt();"; //"int32_t";
                }
                break;
            case UINT32:
                if (forEmbeddedJava) {
                    value = value + " = (int)dis.readInt()&0x00FFFFFFFF;"; //"uint32_t";
                }
                else {
                    value = value + " = (int)dis.getInt()&0x00FFFFFFFF;"; //"uint32_t";
                }
                break;
            case INT64:
                if (forEmbeddedJava) {
                    value = value + " = (long)dis.readLong();"; //"int64_t";
                }
                else {
                    value = value + " = (long)dis.getLong();"; //"int64_t";
                }
                break;
            case UINT64:
                if (forEmbeddedJava) {
                    value = value + " = (long)dis.readLong();"; //"uint64_t";
                }
                else {
                    value = value + " = (long)dis.getLong();"; //"uint64_t";
                }
                break;
            case DOUBLE:
                if (forEmbeddedJava) {
                    value = value + " = (double)dis.readDouble();"; //"double";
                }
                else {
                    value = value + " = (double)dis.getDouble();"; //"double";
                }
                break;
            case FLOAT:
                if (forEmbeddedJava) {
                    value = value + " = (float)dis.readFloat();"; //"float";
                }
                else {
                    value = value + " = (float)dis.getFloat();"; //"float";
                }
                break;
            default:
        }
        value = value + "\n";
        value = value + endLoop;

        return value;
    }

    /**
     * Return code in a String to write the field in byte buffer.
     * Use different Java API if code is embedded
     * @param type name
     * @param forEmbeddedJava true if code must be embedded
     * @return the code writer for the field in a String
     */
    public String getWriteType(String name, boolean forEmbeddedJava) {
        String value = "";
        String arrayString = "";
        String beginLoop = "";
        String endLoop = "  }\n";
        int length = arrayLenth;
        if (!isArray) {
            length = 1;
            arrayString = "";
            endLoop = "";
        }
        else {
            arrayString = "[i]";
            beginLoop = "  for (int i=0; i<" + length + "; i++) {\n";
            value = beginLoop;
        }

        value = value + "  " + (isArray ? "  " : "");
        switch (type) {
            case ARRAY:
            case CHAR:
                if (forEmbeddedJava) {
                    value = value + "dos.writeByte(" + name + arrayString + ");"; //"char"
                }
                else {
                    value = value + "dos.put((byte)(" + name + arrayString + "));"; //"char";
                }
                break;
            case UINT8:
                if (forEmbeddedJava) {
                    value = value + "dos.writeByte(" + name + arrayString + "&0x00FF);"; //"uint8_t";
                }
                else {
                    value = value + "dos.put((byte)(" + name + arrayString + "&0x00FF));"; //"uint8_t";
                }
                break;
            case INT8:
                if (forEmbeddedJava) {
                    value = value + "dos.write(" + name + arrayString + "&0x00FF);"; //"int8_t";
                }
                else {
                    value = value + "dos.put((byte)(" + name + arrayString + "&0x00FF));"; //"int8_t";
                }
                break;
            case INT16:
                if (forEmbeddedJava) {
                    value = value + "dos.writeShort(" + name + arrayString + "&0x00FFFF);"; //"int16_t";
                }
                else {
                    value = value + "dos.putShort((short)(" + name + arrayString + "&0x00FFFF));"; //"int16_t";
                }
                break;
            case UINT16:
                if (forEmbeddedJava) {
                    value = value + "dos.writeShort(" + name + arrayString + "&0x00FFFF);"; //"uint16_t";
                }
                else {
                    value = value + "dos.putShort((short)(" + name + arrayString + "&0x00FFFF));"; //"uint16_t";
                }
                break;
            case INT32:
                if (forEmbeddedJava) {
                    value = value + "dos.writeInt((int)(" + name + arrayString + "&0x00FFFFFFFF));"; //"int32_t";
                }
                else {
                    value = value + "dos.putInt((int)(" + name + arrayString + "&0x00FFFFFFFF));"; //"int32_t";
                }
                break;
            case UINT32:
                if (forEmbeddedJava) {
                    value = value + "dos.writeInt((int)(" + name + arrayString + "&0x00FFFFFFFF));"; //"uint32_t";
                }
                else {
                    value = value + "dos.putInt((int)(" + name + arrayString + "&0x00FFFFFFFF));"; //"uint32_t";
                }
                break;
            case INT64:
                if (forEmbeddedJava) {
                    value = value + "dos.writeLong(" + name + arrayString + ");"; //"int64_t";
                }
                else {
                    value = value + "dos.putLong(" + name + arrayString + ");"; //"int64_t";
                }
                break;
            case UINT64:
                if (forEmbeddedJava) {
                    value = value + "dos.writeLong(" + name + arrayString + ");"; //"uint64_t";
                }
                else {
                    value = value + "dos.putLong(" + name + arrayString + ");"; //"uint64_t";
                }
                break;
            case DOUBLE:
                if (forEmbeddedJava) {
                    value = value + "dos.writeDouble(" + name + arrayString + ");"; //"double";
                }
                else {
                    value = value + "dos.putDouble(" + name + arrayString + ");"; //"double";
                }
                break;
            case FLOAT:
                if (forEmbeddedJava) {
                    value = value + "dos.writeFloat(" + name + arrayString + ");"; //"float";
                }
                else {
                    value = value + "dos.putFloat(" + name + arrayString + ");"; //"float";
                }
                break;
            default:
        }
        value = value + "\n";
        value = value + endLoop;

        return value;
    }
}
