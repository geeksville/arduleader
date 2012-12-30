I) INTRODUCTION
===============

MAVLink -- Micro Air Vehicle Message Marshalling Library.

This is a library for lightweight communication between Micro Air Vehicles (swarm) and/or ground control stations.
It serializes Java classes for serial channels and can be used with any type of radio modem.

Messages definitions are created in XML, and then converted into Java files.
MAVLink is licensed under the terms of the Lesser General Public License of the Free Software Foundation (LGPL). 
As MAVLink is a header-only library, compiling an application with it is considered "using the libary", not a derived work. 
MAVLink can therefore be used without limits in any closed-source application without publishing the source code of the closed-source application.

This MAVLink Java generator uses MAVLink xml definition files to generate Java classes files.
See the readme in org.mavlink.library to use MAVLink in your Java program. Here is only the use of MAVLinl Java generator.

It is architectured with 3 Eclipse Java projects.
org.mavlink.generator : contains the generator and MAVLink xml files. Generated code is put in org.mavlink.library/generated folder.
org.mavlink.library : Helpers for MAVLink and message. Contains generated code from generator.
org.mavlink.util : CRC classes uses by generator and library.
 
After code generation, 2 jar descriptors in org.mavlink.library and org.mavlink.util projects can be used to generate jars for your application...

II) USAGE
=========

Put desired mavlink xml files in a directory. Don't forget include files.
By example for ardupilotmega generation put ardupilotmega.xml and common.xml in a directory

Then generate code in directory "generated" in org.mavlink.library Eclipse project.

So build org.mavlink.library and org.mavlink.util Eclipse project and generate jar with each jardesc in projects.

Now you can use the 2 generated jar in your projects!

Command line arguments of MAVLink Java generator are :
    source : xml file or directory path containing xml files to parse for generation
    target : directory path for output Java source files
    isLittleEndian : true if type are exchanged or stored in LittleEndian in buffer, false for BigEndian
    forEmbeddedJava : true if generated code must use apis for embedded code (CLDC), false else for ground station
    useExtraByte : if true use extra crc byte to compute CRC : true for MAVLink 1.0, false for 0.9
    debug : true to generate toString methods in each message class
    
Example :
    java org.mavlink.generator.MAVLinkGenerator resources/v1.0 target/ true true true true
    java org.mavlink.generator.MAVLinkGenerator resources/v1.0/ardupilotmega.xml target/ true true true true
    Generate MAVLink message Java classes for mavlink xml files contains in resources/v1.0 in target diretory for Little Endian data, embedded code with debug code.
 
 