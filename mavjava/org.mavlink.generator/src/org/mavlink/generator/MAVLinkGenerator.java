/**
 * $Id: MAVLinkGenerator.java 667 2012-11-16 13:30:24Z ghelle $
 * $Date: 2012-11-16 14:30:24 +0100 (ven., 16 nov. 2012) $
 * 
 * ======================================================
 * Copyright (C) 2012 Guillaume Helle.
 * Project : MAVLink Java Generator
 * Module : org.mavlink.generator
 * File : org.mavlink.generator.MAVLinkGenerator.java
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mavlink.MAVLinkCRC;
import org.xml.sax.SAXException;

/**
 * MAVLink Java generator.
 * @author ghelle
 * @version $Rev: 667 $
 * @see main
 */
public class MAVLinkGenerator {

    public final static String MAVLINK_MSG = "MAVLINK_MSG";

    private String imports = "";

    protected boolean debug = true;

    protected boolean forEmbeddedJava = true;

    protected boolean isLittleEndian = true;

    protected boolean useExtraByte = true;

    protected String source = "resources/v1.0/";

    protected String target = "target";

    public static int[] MAVLINK_MESSAGE_CRCS = new int[256];

    /**
     * Main class for the generator.
     * Command line arguments are :
     *   source : directory path containing xml files to parse for generation
     *   target : directory path for output Java source files
     *   isLittleEndian : true if type are stored in LittleEndian in buffer, false for BigEndian
     *   forEmbeddedJava : true if generated code must use apis for embedded code, false else
     *   useExtraByte : if true use extra crc byte to compute CRC
     *   debug : true to generate toString methods in each message class
     *   
     * Example :
     *   java org.mavlink.generator.MAVLinkGenerator  resources/1.0 target/ true true true true
     *   Generate MAVLink message Java classes for mavlink xml files contains in resources/1.0 
     *   in target diretory for Little Endian data, embedded code using extra byte for crc and generating debug code
     * @param args
     */
    public static void main(String[] args) {

        MAVLinkGenerator generator = new MAVLinkGenerator();
        if (args.length != 6) {
            generator.usage(args);
            System.exit(-1);
        }
        generator.source = args[0];
        generator.target = args[1];
        generator.isLittleEndian = Boolean.parseBoolean(args[2]);
        generator.forEmbeddedJava = Boolean.parseBoolean(args[3]);
        generator.useExtraByte = Boolean.parseBoolean(args[4]);
        generator.debug = Boolean.parseBoolean(args[5]);

        File src = new File(generator.source);
        if (src.isDirectory()) {
            generator.parseDirectory(generator.source);
        }
        else if (src.isFile()) {
            generator.parseFile(generator.source, generator.target);
        }
        else {

        }

        System.exit(0);
    }

    /**
     * Usage method to display manual on console.
     */
    public void usage(String[] args) {
        System.out.println("Wrong number of arguments for the generator : " + args.length);
        for (int i = 0; i < args.length; i++)
            System.out.println(i + args[i]);
        System.out.println("Command line arguments are :");
        System.out.println("  source : directory path containing xml files to parse for generation");
        System.out.println("  target : directory path for output Java source files");
        System.out.println("  isLittleEndian : true if type are stored in LittleEndian in buffer, false for BigEndian");
        System.out.println("  forEmbeddedJava : true if generated code must use apis for embedded code, false else");
        System.out.println("  useExtraByte : if true use extra crc byte to compute CRC");
        System.out.println("  debug : true to generate toString methods in each message class");
        System.out.println("  ");
        System.out.println("Example :");
        System.out.println("  java org.mavlink.generator.MAVLinkGenerator  resources/v1.0 target/ true true true");
        System.out.println("  Generate MAVLink message Java classes for mavlink xml files contains in resources/v1.0 ");
        System.out.println("  in target diretory for Little Endian data, embedded code with debug code");
    }

    /**
     * Parse all xml files in directory and call generators methods
     * @param path
     */
    protected void parseDirectory(String path) {
        File directory = new File(path);
        String files[] = directory.list();
        String file = "";
        try {
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".xml")) {
                    int index = files[i].indexOf('.');
                    String rep = target + File.separator + files[i].substring(0, index);
                    parseFile(files[i], path, rep);
                    //                    MAVLinkData mavlink = null;
                    //                    mavlink = new MAVLinkData();
                    //
                    //                    Map<String, String> implementations = parseFile(mavlink, files[i], path, rep, false);
                    //
                    //                    generateMAVLinkClass(rep, implementations);
                    //                    generateFactoryClass(mavlink, rep);
                    //                    generateIMavlinkId(mavlink, rep);
                    //                    generateMavlinkCoder(mavlink, rep);
                    //                    generateIMavlinkCRC(rep);
                    //                    imports = "";
                }
            }
        }
        catch (Exception e) {
            System.err.println("MAVLinkGenerator Error : " + file + "  =  " + e);
            e.printStackTrace();
        }
    }

    protected void parseFile(String filename, String dir, String destination) {
        try {
            File src = new File(filename);
            String path = src.getAbsolutePath();
            System.out.println("Parse : " + filename + " in directory : " + dir + " to directory :" + destination);
            MAVLinkData mavlink = null;
            mavlink = new MAVLinkData();
            Map<String, String> implementations = parseFile(mavlink, filename, dir, destination, false);

            generateMAVLinkClass(destination, implementations);
            generateFactoryClass(mavlink, destination);
            generateIMavlinkId(mavlink, destination);
            generateMavlinkCoder(mavlink, destination);
            generateIMavlinkCRC(destination);
            imports = "";
        }
        catch (Exception e) {
            System.err.println("MAVLinkGenerator Error : " + filename + "  =  " + e);
            e.printStackTrace();
        }
    }

    protected void parseFile(String filename, String destination) {
        String file = filename.substring(filename.lastIndexOf(File.separator) + 1);
        String dir = filename.substring(0, filename.lastIndexOf(File.separator));
        parseFile(file, dir, destination);
    }

    /**
     * Parse a MAVLink xml messages descriptor file.
     * @param mavlink MAVLink data used and to fill
     * @param file Parsed file
     * @param path Path to file
     * @param target Path for generation
     * @param inInclude True if we are in an include file
     * @return implementation code for readers and writers.
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Map<String, String> parseFile(MAVLinkData mavlink, String file, String path, String target, boolean inInclude)
            throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> implementations = new HashMap<String, String>();
        SAXParserFactory fabrique = SAXParserFactory.newInstance();
        SAXParser parseur = fabrique.newSAXParser();
        if (!inInclude) {
            mavlink.setFile(file.substring(0, file.indexOf('.')));
            System.out.println("MAVLinkData : " + mavlink.getFile());
        }
        else {
            System.out.println("MAVLinkData INCLUDE : " + file.substring(0, file.indexOf('.')));
        }
        MAVLinkHandler gestionnaire = new MAVLinkHandler(this, mavlink, path, target);
        parseur.parse(new File(path + File.separator + file), gestionnaire);
        mavlink = gestionnaire.getMavlink();
        generateMessageClass(mavlink, target);
        mavlink.getEnums().putAll(mavlink.getEnums());
        mavlink.getMessages().putAll(mavlink.getMessages());
        generateEnumClass(mavlink, target, implementations);
        return implementations;
    }

    /**
     * Generate MAVLink messages Java classes
     * @param mavlink
     * @param targetPath
     */
    protected void generateMessageClass(MAVLinkData mavlink, String targetPath) {
        StringBuffer sbRead, sbWrite, fieldWrite;
        String packageRootName = "org.mavlink.messages";
        String xmlFilename = mavlink.getFile();
        String packageName = packageRootName + "." + xmlFilename;
        String directory = targetPath + "/org/mavlink/messages/" + xmlFilename + "/";
        OutputStream output = null;
        PrintWriter writer = null;
        String forToString = "";
        for (MAVLinkMessage message : mavlink.getMessages().values()) {
            String className = "msg_" + message.getName().toLowerCase();
            String filename = directory + className + ".java";
            imports = imports + "import " + packageName + "." + className + ";\n";
            try {
                File file = new File(directory);
                file.mkdirs();
                output = new FileOutputStream(filename, false);
                writer = new PrintWriter(output);
                sbRead = new StringBuffer();
                sbWrite = new StringBuffer();
                fieldWrite = new StringBuffer();
                if (forEmbeddedJava) {
                    sbWrite.append("  dos.writeByte((byte)0xFE);\n");
                    sbWrite.append("  dos.writeByte(length & 0x00FF);\n");
                    sbWrite.append("  dos.writeByte(sequence & 0x00FF);\n");
                    sbWrite.append("  dos.writeByte(sysId & 0x00FF);\n");
                    sbWrite.append("  dos.writeByte(componentId & 0x00FF);\n");
                    sbWrite.append("  dos.writeByte(messageType & 0x00FF);\n");
                }
                else {
                    sbWrite.append("  dos.put((byte)0xFE);\n");
                    sbWrite.append("  dos.put((byte)(length & 0x00FF));\n");
                    sbWrite.append("  dos.put((byte)(sequence & 0x00FF));\n");
                    sbWrite.append("  dos.put((byte)(sysId & 0x00FF));\n");
                    sbWrite.append("  dos.put((byte)(componentId & 0x00FF));\n");
                    sbWrite.append("  dos.put((byte)(messageType & 0x00FF));\n");
                }
                // Write Header
                writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
                writer.print("package " + packageName + ";\n");
                writer.print("import " + packageRootName + ".MAVLinkMessage;\n");
                writer.print("import org.mavlink.IMAVLinkCRC;\n");
                writer.print("import org.mavlink.MAVLinkCRC;\n");
                writer.print("import java.io.ByteArrayOutputStream;\n");
                writer.print("import java.io.IOException;\n");
                if (forEmbeddedJava) {
                    if (isLittleEndian) {
                        writer.print("import org.mavlink.io.LittleEndianDataInputStream;\n");
                        writer.print("import org.mavlink.io.LittleEndianDataOutputStream;\n");
                    }
                    else {
                        writer.print("import java.io.DataInputStream;\n");
                        writer.print("import java.io.DataOutputStream;\n");
                    }
                }
                else {
                    writer.print("import java.nio.ByteBuffer;\n");
                    writer.print("import java.nio.ByteOrder;\n");
                }
                String description = message.getDescription();
                writer.print("/**\n * Class " + className + "\n * " + (description == null ? "" : message.getDescription().trim()) + "\n **/\n");
                writer.print("public class " + className + " extends MAVLinkMessage {\n");
                String id = MAVLINK_MSG + "_ID_" + message.getName();
                writer.print("  public static final int " + id + " = " + message.getId() + ";\n");
                writer.print("  private static final long serialVersionUID = " + id + ";\n");
                writer.print("  public " + className + "(int sysId, int componentId) {\n    messageType = " + id
                             + ";\n    this.sysId = sysId;\n    this.componentId = componentId;\n");

                // Calculate extra_crc for Mavlinl 1.0
                String extraCrcBuffer = message.getName() + " ";
                // Write Fields
                int fieldLen = 0;
                Collections.sort(message.getFields(), new FieldCompare());
                for (int j = 0; j < message.getFields().size(); j++) {
                    MAVLinkField field = message.getFields().get(j);
                    fieldWrite.append("  /**\n   * " + field.getDescription().trim() + "\n   */\n");
                    MAVLinkDataType type = field.getType();
                    fieldWrite.append("  public " + type.getJavaType(field.getName()) + "\n");
                    sbRead.append(type.getReadType(field.getName(), forEmbeddedJava));
                    sbWrite.append(type.getWriteType(field.getName(), forEmbeddedJava));
                    String attr = field.getName();
                    if (type.isArray && type.type == MAVLinkDataType.CHAR) {
                        String first = "" + attr.charAt(0);
                        attr = first.toUpperCase() + field.getName().substring(1);
                        fieldWrite.append("  public void set" + attr + "(String tmp) {\n");
                        fieldWrite.append("    int len = Math.min(tmp.length(), " + type.arrayLenth + ");\n");
                        fieldWrite.append("    for (int i=0; i<len; i++) {\n      " + field.getName() + "[i] = tmp.charAt(i);\n    }\n");
                        fieldWrite.append("    for (int i=len; i<" + type.arrayLenth + "; i++) {\n      " + field.getName()
                                          + "[i] = 0;\n    }\n  }\n");
                        fieldWrite.append("  public String get" + attr + "() {\n");
                        fieldWrite.append("    String result=\"\";\n");
                        fieldWrite.append("    for (int i=0; i<" + type.arrayLenth + "; i++) {\n      if (" + field.getName()
                                          + "[i] != 0) result=result+" + field.getName() + "[i]; else break;\n    }\n    return result;\n  }\n");
                    }
                    fieldLen += type.getLengthType();
                    forToString = forToString + (j != 0 ? "+" : "") + "  \"  " + field.getName() + "=\"+"
                                  + (field.getType().isArray && field.getType().type == MAVLinkDataType.CHAR ? "get" + attr + "()" : field.getName());
                    extraCrcBuffer = extraCrcBuffer + type.getCType() + " " + field.getName() + " ";
                    if (type.isArray) {
                        extraCrcBuffer = extraCrcBuffer + (char) type.arrayLenth;
                    }
                }
                writer.print("    length = " + fieldLen + ";\n}\n\n");
                writer.print(fieldWrite.toString());
                int extra_crc = MAVLinkCRC.crc_calculate(MAVLinkCRC.stringToByte(extraCrcBuffer));
                int magicNumber = (extra_crc & 0x00FF) ^ ((extra_crc >> 8 & 0x00FF));
                MAVLINK_MESSAGE_CRCS[message.getId()] = magicNumber;

                writer.print("/**\n");
                writer.print(" * Decode message with raw data\n");
                writer.print(" */\n");
                if (forEmbeddedJava) {
                    if (isLittleEndian) {
                        writer.print("public void decode(LittleEndianDataInputStream dis) throws IOException {\n");
                    }
                    else {
                        writer.print("public void decode(DataInputStream dis) throws IOException {\n");
                    }
                }
                else {
                    writer.print("public void decode(ByteBuffer dis) throws IOException {\n");
                }

                writer.print(sbRead.toString());
                writer.print("}\n");

                writer.print("/**\n");
                writer.print(" * Encode message with raw data and other informations\n");
                writer.print(" */\n");
                writer.print("public byte[] encode() throws IOException {\n");
                writer.print("  byte[] buffer = new byte[8+" + fieldLen + "];\n");
                if (forEmbeddedJava) {
                    if (isLittleEndian) {
                        writer.print("   LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(new ByteArrayOutputStream());\n");
                    }
                    else {
                        writer.print("   ByteArrayOutputStream baos = new ByteArrayOutputStream();\n");
                        writer.print("   DataOutputStream dos = new DataOutputStream(baos);\n");
                    }
                }
                else {
                    if (isLittleEndian) {
                        writer.print("   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);\n");
                    }
                    else {
                        writer.print("   ByteBuffer dos = ByteBuffer.wrap(buffer).order(ByteOrder.BIG_ENDIAN);\n");
                    }
                }
                writer.print(sbWrite.toString());
                if (forEmbeddedJava) {
                    if (isLittleEndian) {
                        writer.print("  dos.flush();\n  byte[] tmp = dos.toByteArray();\n");
                    }
                    else {
                        writer.print("  dos.flush();\n  byte[] tmp = baos.toByteArray();\n");
                    }
                    writer.print("  for (int b=0; b<tmp.length; b++) buffer[b]=tmp[b];\n");
                }
                else {
                    // nothing
                }
                writer.print("  int crc = MAVLinkCRC.crc_calculate_encode(buffer, " + fieldLen + ");\n");
                writer.print("  crc = MAVLinkCRC.crc_accumulate((byte) IMAVLinkCRC.MAVLINK_MESSAGE_CRCS[messageType], crc);\n");
                writer.print("  byte crcl = (byte) (crc & 0x00FF);\n");
                writer.print("  byte crch = (byte) ((crc >> 8) & 0x00FF);\n");
                writer.print("  buffer[" + (fieldLen + 6) + "] = crcl;\n");
                writer.print("  buffer[" + (fieldLen + 7) + "] = crch;\n");
                writer.print("  return buffer;\n}\n");

                if (debug) {
                    writer.print("public String toString() {\n");
                    writer.print("return \"" + id + " : \" + " + forToString + ";");
                    writer.print("}\n");
                }
                writer.print("}\n");
                forToString = "";
            }
            catch (Exception e) {
                System.err.println("ERROR : " + e);
                e.printStackTrace();
            }
            finally {
                try {
                    writer.close();
                    output.close();
                }
                catch (Exception ex) {
                    System.err.println("ERROR : " + ex);
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Generate MAVLink Java Enum classes
     * @param mavlink
     * @param targetPath
     * @param implementations
     */
    protected void generateEnumClass(MAVLinkData mavlink, String targetPath, Map<String, String> implementations) {
        String packageRootName = "org.mavlink.messages";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/messages/";
        OutputStream output = null;
        PrintWriter writer = null;

        for (MAVLinkEnum message : mavlink.getEnums().values()) {
            try {
                String className = message.getName();
                String filename = directory + className + ".java";
                output = new FileOutputStream(filename, false);
                writer = new PrintWriter(output);
                writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
                writer.print("package " + packageName + ";\n");
                String description = message.getDescription();
                writer.print("/**\n * Interface " + className + "\n * " + (description == null ? "" : message.getDescription().trim()) + "\n **/\n");
                writer.print("public interface " + className + " {\n");
                implementations.put(className.trim(), className.trim());
                for (int j = 0; j < message.getEntries().size(); j++) {
                    MAVLinkEntry entry = message.getEntries().get(j);
                    writer.print("    /**\n     * " + entry.getDescription() + "\n");
                    for (int k = 0; k < entry.getParams().size(); k++) {
                        MAVLinkParam param = entry.getParams().get(k);
                        writer.print("     * PARAM " + param.getIndex() + " : " + param.getComment() + "\n");
                    }
                    writer.print("     */\n");
                    writer.print("    public final static int " + entry.getName() + " = " + entry.getValue() + ";\n");
                }
                writer.print("}\n");
            }
            catch (Exception e) {
                System.err.println("ERROR : " + e);
                e.printStackTrace();
            }
            finally {
                try {
                    writer.close();
                    output.close();
                }
                catch (Exception ex) {
                    System.err.println("ERROR : " + ex);
                    ex.printStackTrace();
                }
            }

        }
    }

    /**
     * Generate Interface which extends all enums classes
     * @param targetPath
     * @param implementation
     */
    protected void generateMAVLinkClass(String targetPath, Map<String, String> implementation) {
        String packageRootName = "org.mavlink.messages";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/messages/";
        OutputStream output = null;
        PrintWriter writer = null;
        String allImpl = "";
        for (String impl : implementation.values()) {
            allImpl = allImpl + impl + ",";
        }
        try {
            String className = "IMAVLink";
            String filename = directory + className + ".java";
            output = new FileOutputStream(filename, false);
            writer = new PrintWriter(output);
            writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
            writer.print("package " + packageName + ";\n");
            writer.print("/**\n * Interface " + className + "\n * Implement all constants in enums and entries \n **/\n");
            if (implementation.size() != 0) {
                writer.print("public interface " + className + " extends " + allImpl.substring(0, allImpl.length() - 1) + " {\n}\n");
            }
            else {
                writer.print("public interface " + className + " {\n}\n");
            }
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
            System.err.println("allImpl : " + allImpl);
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
                output.close();
            }
            catch (Exception ex) {
                System.err.println("ERROR : " + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generate a factory classe which generate MAVLink messages from byte array
     * @param mavlink
     * @param targetPath
     */
    protected void generateFactoryClass(MAVLinkData mavlink, String targetPath) {
        String packageRootName = "org.mavlink.messages";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/messages/";
        OutputStream output = null;
        PrintWriter writer = null;
        String className = "MAVLinkMessageFactory";
        String filename = directory + className + ".java";
        try {
            File file = new File(directory);
            file.mkdirs();
            output = new FileOutputStream(filename, false);
            writer = new PrintWriter(output);
            // Write Header
            writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
            writer.print("package " + packageName + ";\n");
            writer.print("import " + packageRootName + ".MAVLinkMessage;\n");
            writer.print("import org.mavlink.IMAVLinkMessage;\n");
            writer.print("import java.io.IOException;\n");
            if (forEmbeddedJava) {
                if (isLittleEndian) {
                    writer.print("import org.mavlink.io.LittleEndianDataInputStream;\n");
                    writer.print("import java.io.ByteArrayInputStream;\n");
                }
                else {
                    writer.print("import java.io.DataInputStream;\n");
                    writer.print("import java.io.ByteArrayInputStream;\n");
                }
            }
            else {
                writer.print("import java.nio.ByteBuffer;\n");
                writer.print("import java.nio.ByteOrder;\n");
            }
            writer.print(imports);
            writer.print("/**\n * Class MAVLinkMessageFactory\n * Generate MAVLink message classes from byte array\n **/\n");
            writer.print("public class MAVLinkMessageFactory implements IMAVLinkMessage, IMAVLinkMessageID {\n");
            writer.print("public static MAVLinkMessage getMessage(int msgid, int sysId, int componentId, byte[] rawData) throws IOException {\n");
            writer.print("    MAVLinkMessage msg=null;\n");
            if (forEmbeddedJava) {
                if (isLittleEndian) {
                    writer.print("    LittleEndianDataInputStream dis = new LittleEndianDataInputStream(new ByteArrayInputStream(rawData));\n");
                }
                else {
                    writer.print("    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(rawData));\n");
                }
            }
            else {
                if (isLittleEndian) {
                    writer.print("    ByteBuffer dis = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);\n");
                }
                else {
                    writer.print("    ByteBuffer dis = ByteBuffer.wrap(rawData).order(ByteOrder.BIG_ENDIAN);\n");
                }
            }
            writer.print("    switch(msgid) {\n");
            for (MAVLinkMessage message : mavlink.getMessages().values()) {
                String msgClassName = "msg_" + message.getName().toLowerCase();
                String id = MAVLINK_MSG + "_ID_" + message.getName();
                writer.print("  case " + id + ":\n");
                writer.print("      msg = new " + msgClassName + "(sysId, componentId);\n");
                writer.print("      msg.decode(dis);\n");
                writer.print("      break;\n");
            }
            writer.print("  default:\n");
            writer.print("      System.out.println(\"Mavlink Factory Error : unknown MsgId : \" + msgid);\n");
            writer.print("    }\n");
            writer.print("    return msg;\n");
            writer.print("  }\n");
            writer.print("}\n");
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
                output.close();
            }
            catch (Exception ex) {
                System.err.println("ERROR : " + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generate Interface with all MAVLink messages ID
     * @param mavlink
     * @param targetPath
     */
    protected void generateIMavlinkId(MAVLinkData mavlink, String targetPath) {
        String packageRootName = "org.mavlink.messages";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/messages/";
        OutputStream output = null;
        PrintWriter writer = null;
        String className = "IMAVLinkMessageID";
        String filename = directory + className + ".java";
        try {
            File file = new File(directory);
            file.mkdirs();
            output = new FileOutputStream(filename, false);
            writer = new PrintWriter(output);
            // Write Header
            writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
            writer.print("package " + packageName + ";\n");
            writer.print("/**\n * Interface IMAVLinkMessageId\n * Generate al MAVLink message Id in an interface\n **/\n");
            writer.print("public interface IMAVLinkMessageID {\n");
            for (MAVLinkMessage message : mavlink.getMessages().values()) {
                String id = MAVLINK_MSG + "_ID_" + message.getName();
                writer.print("  public static int " + id + " = " + message.getId() + ";\n");
            }
            writer.print("}\n");
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
                output.close();
            }
            catch (Exception ex) {
                System.err.println("ERROR : " + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generate interface with all extra crc for messages
     * @param targetPath
     */
    protected void generateIMavlinkCRC(String targetPath) {
        String packageRootName = "org.mavlink";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/";
        OutputStream output = null;
        PrintWriter writer = null;
        String className = "IMAVLinkCRC";
        String filename = directory + className + ".java";
        try {
            File file = new File(directory);
            file.mkdirs();
            output = new FileOutputStream(filename, false);
            writer = new PrintWriter(output);
            // Write Header
            writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
            writer.print("package " + packageName + ";\n");
            writer.print("/**\n * Interface IMAVLinkCRC\n * Extra byte to compute CRC in Mavlink 1.0\n **/\n");
            writer.print("public interface IMAVLinkCRC {\n");
            if (useExtraByte) {
                writer.print("  public static boolean MAVLINK_EXTRA_CRC = true;\n");
            }
            else {
                writer.print("  public static boolean MAVLINK_EXTRA_CRC = false;\n");
            }
            writer.print("  public static char[] MAVLINK_MESSAGE_CRCS = {\n");
            for (int i = 0; i < MAVLINK_MESSAGE_CRCS.length; i++) {
                if (i % 25 == 0)
                    writer.print("\n          ");
                writer.print(MAVLINK_MESSAGE_CRCS[i]);
                if (i != MAVLINK_MESSAGE_CRCS.length - 1)
                    writer.print(", ");
            }
            writer.print("};\n}");
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
                output.close();
            }
            catch (Exception ex) {
                System.err.println("ERROR : " + ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Generate encode and decode methods for all MAVLink messages
     * @param mavlink
     * @param targetPath
     */
    protected void generateMavlinkCoder(MAVLinkData mavlink, String targetPath) {
        String packageRootName = "org.mavlink.messages";
        String packageName = packageRootName;
        String directory = targetPath + "/org/mavlink/messages/";
        OutputStream output = null;
        PrintWriter writer = null;
        String className = "MAVLinkMessageCoder";
        String filename = directory + className + ".java";
        try {
            File file = new File(directory);
            file.mkdirs();
            output = new FileOutputStream(filename, false);
            writer = new PrintWriter(output);
            // Write Header
            writer.print("/**\n * Generated class : " + className + "\n * DO NOT MODIFY!\n **/\n");
            writer.print("package " + packageName + ";\n");
            writer.print("import java.io.IOException;\n");
            if (forEmbeddedJava) {
                if (isLittleEndian) {
                    writer.print("import org.mavlink.io.LittleEndianDataInputStream;\n");
                }
                else {
                    writer.print("import java.io.DataInputStream;\n");
                }
            }
            else {
                writer.print("import java.io.Serializable;");
                writer.print("import java.nio.ByteBuffer;\n");
                writer.print("import java.nio.ByteOrder;\n");
            }
            writer.print("/**\n * Class MAVLinkMessageCoder\n * Use to declarate encode and decode functions\n **/\n");
            writer.print("public abstract class MAVLinkMessageCoder ");
            if (forEmbeddedJava) {
                writer.print("{\n");
            }
            else {
                writer.print(" implements Serializable{\n");
            }
            writer.print("  /**\n");
            writer.print("   * Decode message with raw data\n");
            writer.print("   */\n");
            if (forEmbeddedJava) {
                if (isLittleEndian) {
                    writer.print("  public abstract void decode(LittleEndianDataInputStream dis) throws IOException ;\n");
                }
                else {
                    writer.print("  public abstract void decode(DataInputStream dis) throws IOException ;\n");
                }
            }
            else {
                writer.print("  public abstract void decode(ByteBuffer dis) throws IOException ;\n");
            }
            writer.print("  /**\n");
            writer.print("   * Encode message in raw data\n");
            writer.print("   */\n");
            if (forEmbeddedJava) {
                if (isLittleEndian) {
                    writer.print("  public abstract byte[] encode() throws IOException ;\n");
                }
                else {
                    writer.print("  public abstract byte[] encode() throws IOException ;\n");
                }
            }
            else {
                writer.print("  public abstract byte[] encode() throws IOException ;\n");
            }
            writer.print("}\n");
        }
        catch (Exception e) {
            System.err.println("ERROR : " + e);
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
                output.close();
            }
            catch (Exception ex) {
                System.err.println("ERROR : " + ex);
                ex.printStackTrace();
            }
        }
    }
}
