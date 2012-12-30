I) INTRODUCTION
===============

MAVLink -- Micro Air Vehicle Message Marshalling Library.

This is a library for lightweight communication between Micro Air Vehicles (swarm) and/or ground control stations.
It serializes Java classes for serial channels and can be used with any type of radio modem.

Messages definitions are created in XML, and then converted into Java files.
MAVLink is licensed under the terms of the Lesser General Public License of the Free Software Foundation (LGPL). 
As MAVLink is a header-only library, compiling an application with it is considered "using the libary", not a derived work. 
MAVLink can therefore be used without limits in any closed-source application without publishing the source code of the closed-source application.

This MAVLink Java library is generated from MAVLink xml definition files.

It is architectured with 2 Eclipse Java projects.
org.mavlink.library : Helpers for MAVLink and message. Contains generated code from generator.
org.mavlink.util : CRC classes uses by generator and library.
 

II) USAGE
=========

Use MAVLinkReader to read messages with method MAVLinkMessage getNextMessage().
            MAVLinkReader reader = new MAVLinkReader(dis, IMAVLinkMessage.MAVPROT_PACKET_START_V10);
            MAVLinkMessage msg;
            while (true) {
                msg = reader.getNextMessage();
                if (msg != null) {
                	// Do your stuff
                	...
                }
			}

Use encode() method on MAVLink message to generate a byte buffer so you can send it in a Data Output Stream.
                        msg_heartbeat hb = new msg_heartbeat(sysId, componentId);
                        hb.sequence = sequence++;
                        hb.autopilot = autopilot;
                        hb.base_mode = base_mode;
                        hb.custom_mode = custom_mode;
                        hb.mavlink_version = mavlink_version;
                        hb.system_status = system_status;
                        hb.type = type;
                        byte[] result = hb.encode();
                        dos.put(result);
