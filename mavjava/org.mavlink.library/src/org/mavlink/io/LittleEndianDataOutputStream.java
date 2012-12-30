package org.mavlink.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Little-endian version of DataOutputStream.
 */
public final class LittleEndianDataOutputStream extends OutputStream implements DataOutput {

    protected final DataOutputStream dis;

    protected final ByteArrayOutputStream baos;

    protected final byte[] work;

    public LittleEndianDataOutputStream(OutputStream out) {
        this.dis = new DataOutputStream(out);
        work = new byte[8];
        baos = (ByteArrayOutputStream) out;
    }

    public final byte[] toByteArray() {
        return baos.toByteArray();
    }

    public final void close() throws IOException {
        dis.close();
    }

    public void flush() throws IOException {
        dis.flush();
    }

    public final synchronized void write(int ib) throws IOException {
        dis.write(ib);
    }

    public final void write(byte ba[]) throws IOException {
        dis.write(ba, 0, ba.length);
    }

    public final synchronized void write(byte ba[], int off, int len) throws IOException {
        dis.write(ba, off, len);
    }

    public final void writeBoolean(boolean v) throws IOException {
        dis.writeBoolean(v);
    }

    public final void writeByte(int v) throws IOException {
        dis.writeByte(v);
    }

    public final void writeChar(int v) throws IOException {
        work[0] = (byte) v;
        work[1] = (byte) (v >> 8);
        dis.write(work, 0, 2);
    }

    public final void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            writeChar(s.charAt(i));
        }
    }

    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeInt(int v) throws IOException {
        work[0] = (byte) v;
        work[1] = (byte) (v >> 8);
        work[2] = (byte) (v >> 16);
        work[3] = (byte) (v >> 24);
        dis.write(work, 0, 4);
    }

    public final void writeLong(long v) throws IOException {
        work[0] = (byte) v;
        work[1] = (byte) (v >> 8);
        work[2] = (byte) (v >> 16);
        work[3] = (byte) (v >> 24);
        work[4] = (byte) (v >> 32);
        work[5] = (byte) (v >> 40);
        work[6] = (byte) (v >> 48);
        work[7] = (byte) (v >> 56);
        dis.write(work, 0, 8);
    }

    public final void writeShort(int v) throws IOException {
        work[0] = (byte) v;
        work[1] = (byte) (v >> 8);
        dis.write(work, 0, 2);
    }

    public final void writeUTF(String s) throws IOException {
        dis.writeUTF(s);
    }

    public void writeBytes(String s) throws IOException {
        dis.write(s.getBytes());
    }
}
