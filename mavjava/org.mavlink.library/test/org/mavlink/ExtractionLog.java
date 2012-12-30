package org.mavlink;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.mavlink.messages.IMAVLinkMessageID;
import org.mavlink.messages.MAVLinkMessage;
import org.mavlink.messages.ardupilotmega.msg_attitude;
import org.mavlink.messages.ardupilotmega.msg_rc_channels_raw;
import org.mavlink.messages.ardupilotmega.msg_servo_output_raw;

public class ExtractionLog {

    /**
     * @param args
     */
    public static void main(String[] args) {
        MAVLinkReader reader;
        long timeAttitude = 0L, lastTimeAttitude;
        long timeOutput = 0L, lastTimeOutput;
        long timeInput = 0L, lastTimeInput;
        boolean firstAttitude = true, firstOutput = true, firstInput = true;
        ;

        String filename = "data/V1.0.log";

        boolean limitation = true;

        //heure du log
        int hd = 16;
        int md = 44;
        int sd = 03;

        //heure de debut
        int hdeb = 17;
        int mdeb = 2;
        int sdeb = 40;

        //heure de fin
        int hfin = 17;
        int mfin = 3;
        int sfin = 12;

        long debut = tempsMillis(hdeb, mdeb, sdeb, hd, md, sd);
        long fin = tempsMillis(hfin, mfin, sfin, hd, md, sd);

        try {
            PrintWriter attitude = new PrintWriter(new BufferedWriter(new FileWriter(filename + "-attitude_(" + hdeb + "_" + mdeb + "_" + sdeb + "-"
                                                                                     + hfin + "_" + mfin + "_" + sfin + ").csv")));
            PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(filename + "-output_(" + hdeb + "_" + mdeb + "_" + sdeb + "-"
                                                                                   + hfin + "_" + mfin + "_" + sfin + ").csv")));
            PrintWriter input = new PrintWriter(new BufferedWriter(new FileWriter(filename + "-input_(" + hdeb + "_" + mdeb + "_" + sdeb + "-" + hfin
                                                                                  + "_" + mfin + "_" + sfin + ").csv")));

            DataInputStream dis = new DataInputStream(new FileInputStream(filename));
            //reader = new MAVLinkReader(dis, IMAVLinkMessage.MAVPROT_PACKET_START_V09);
            reader = new MAVLinkReader(dis, IMAVLinkMessage.MAVPROT_PACKET_START_V10);

            //Formatage pour la macro excel (Ctrl + maj + G // CreerGraphs)
            attitude.println(";;;;;;;;57,29577951308232;");
            attitude.println();
            output.println(";;;;;;;;1;");
            output.println();
            input.println(";;;;;;;;1;");
            input.println();

            while (dis.available() > 0) {
                MAVLinkMessage msg = reader.getNextMessage();
                if (msg != null && msg.messageType == IMAVLinkMessageID.MAVLINK_MSG_ID_ATTITUDE
                    && ((((msg_attitude) msg).time_boot_ms >= debut && ((msg_attitude) msg).time_boot_ms <= fin) || !limitation)) {
                    if (firstAttitude) {
                        lastTimeAttitude = ((msg_attitude) msg).time_boot_ms;
                        timeAttitude = ((msg_attitude) msg).time_boot_ms;
                        firstAttitude = false;
                    }
                    else {
                        lastTimeAttitude = timeAttitude;
                        timeAttitude = ((msg_attitude) msg).time_boot_ms;
                    }
                    attitude.println(((msg_attitude) msg).roll + ";" + ((msg_attitude) msg).pitch + ";" + ((msg_attitude) msg).yaw + ";"
                                     + ((msg_attitude) msg).rollspeed + ";" + ((msg_attitude) msg).pitchspeed + ";" + ((msg_attitude) msg).yawspeed
                                     + ";" + ((double) (timeAttitude - lastTimeAttitude) / 1000));
                }
                if (msg != null && msg.messageType == IMAVLinkMessageID.MAVLINK_MSG_ID_SERVO_OUTPUT_RAW
                    && ((((msg_servo_output_raw) msg).time_boot_ms >= debut && ((msg_servo_output_raw) msg).time_boot_ms <= fin) || !limitation)) {
                    if (firstOutput) {
                        lastTimeOutput = ((msg_servo_output_raw) msg).time_boot_ms;
                        timeOutput = ((msg_servo_output_raw) msg).time_boot_ms;
                        firstOutput = false;
                    }
                    else {
                        lastTimeOutput = timeOutput;
                        timeOutput = ((msg_servo_output_raw) msg).time_boot_ms;
                    }
                    output.println(((msg_servo_output_raw) msg).servo1_raw + ";" + ((msg_servo_output_raw) msg).servo2_raw + ";"
                                   + ((msg_servo_output_raw) msg).servo3_raw + ";" + ((msg_servo_output_raw) msg).servo4_raw + ";;;"
                                   + ((double) (timeOutput - lastTimeOutput) / 1000));
                }
                if (msg != null && msg.messageType == IMAVLinkMessageID.MAVLINK_MSG_ID_RC_CHANNELS_RAW
                    && ((((msg_rc_channels_raw) msg).time_boot_ms >= debut && ((msg_rc_channels_raw) msg).time_boot_ms <= fin) || !limitation)) {
                    if (firstInput) {
                        lastTimeInput = ((msg_rc_channels_raw) msg).time_boot_ms;
                        timeInput = ((msg_rc_channels_raw) msg).time_boot_ms;
                        firstInput = false;
                    }
                    else {
                        lastTimeInput = timeInput;
                        timeInput = ((msg_rc_channels_raw) msg).time_boot_ms;
                    }
                    input.println(((msg_rc_channels_raw) msg).chan1_raw + ";" + ((msg_rc_channels_raw) msg).chan2_raw + ";"
                                  + ((msg_rc_channels_raw) msg).chan3_raw + ";" + ((msg_rc_channels_raw) msg).chan4_raw + ";;;"
                                  + ((double) (timeInput - lastTimeInput) / 1000));
                }
            }
            attitude.close();
            output.close();
            input.close();
            dis.close();
        }
        catch (Exception e) {
            System.out.println("ERROR : " + e);
        }
    }

    public static long tempsMillis(int h, int m, int s, int hd, int md, int sd) {
        return (long) ((h - hd) * 3600 + (m - md) * 60 + (s - sd)) * 1000L;
    }

}
