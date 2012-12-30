package org.mavlink.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Little-Endian version of DataInputStream.
 */
public final class LittleEndianDataInputStream extends InputStream implements DataInput {

    protected DataInputStream dis;

    protected InputStream is;

    protected byte[] work = null;

    public LittleEndianDataInputStream(InputStream in) {
        this.is = in;
        this.dis = new DataInputStream(in);
        work = new byte[8];
    }

    public final void close() throws IOException {
        dis.close();
    }

    public final int read(byte ba[], int off, int len) throws IOException {
        return is.read(ba, off, len);
    }

    public final boolean readBoolean() throws IOException {
        return dis.readBoolean();
    }

    public final byte readByte() throws IOException {
        return dis.readByte();
    }

    public final char readChar() throws IOException {
        dis.readFully(work, 0, 2);
        return (char) ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final void readFully(byte ba[]) throws IOException {
        dis.readFully(ba, 0, ba.length);
    }

    public final void readFully(byte ba[], int off, int len) throws IOException {
        dis.readFully(ba, off, len);
    }

    public final int readInt() throws IOException {
        dis.readFully(work, 0, 4);
        return (work[3]) << 24 | (work[2] & 0xff) << 16 | (work[1] & 0xff) << 8 | (work[0] & 0xff);
    }

    public final long readLong() throws IOException {
        dis.readFully(work, 0, 8);
        return (long) (work[7]) << 56 |
        /* long cast needed or shift done modulo 32 */
        (long) (work[6] & 0xff) << 48 | (long) (work[5] & 0xff) << 40 | (long) (work[4] & 0xff) << 32 | (long) (work[3] & 0xff) << 24
               | (long) (work[2] & 0xff) << 16 | (long) (work[1] & 0xff) << 8 | (long) (work[0] & 0xff);
    }

    public final short readShort() throws IOException {
        dis.readFully(work, 0, 2);
        return (short) ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    public final String readUTF() throws IOException {
        return dis.readUTF();
    }

    public final int readUnsignedByte() throws IOException {
        return dis.readUnsignedByte();
    }

    public final int readUnsignedShort() throws IOException {
        dis.readFully(work, 0, 2);
        return ((work[1] & 0xff) << 8 | (work[0] & 0xff));
    }

    public final int skipBytes(int n) throws IOException {
        return dis.skipBytes(n);
    }

    public int read() throws IOException {
        return dis.read();
    }

    public String readLine() throws IOException {
        //        return dis.readLine();
        return "";
    }
}
