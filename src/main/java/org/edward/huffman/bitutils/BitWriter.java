package org.edward.huffman.bitutils;

import java.io.IOException;
import java.io.OutputStream;

public class BitWriter {

    private final OutputStream bytes;
    private byte[] buf = new byte[1024];
    private int bitOff;

    public BitWriter(OutputStream bytes) {
        this.bytes = bytes;
        this.bitOff = 0;
    }

    public void writeBit(boolean bit) throws IOException {
        int bufOff = bitOff / 8;
        if (bit) {
            buf[bufOff] = (byte) (buf[bufOff] ^ (0x1 << (bitOff % 8)));
        }
        bitOff++;
        flushOutToStream();
    }

    public void writeByte(byte b) throws IOException {
        if (bitOff % 8 != 0) {
            throw new IllegalStateException("Trying to write unaligned byte");
        }
        buf[bitOff / 8] = b;
        bitOff += 8;
        flushOutToStream();
    }

    public void writeShort(short s) throws IOException {
        byte lower = (byte) (s & 0xFF);
        byte upper = (byte) ((s >> Byte.SIZE) & 0xFF);
        writeByte(lower);
        writeByte(upper);
    }

    private void flushOutToStream() throws IOException {
        if (bitOff / 8 == buf.length) {
            bytes.write(buf);
            bitOff = 0;
            buf = new byte[buf.length];
        }
    }

    public void flush() throws IOException {
        int bytesToWrite = (int) Math.ceil(bitOff / 8.0);
        for (int i = 0; i < bytesToWrite; i++) {
            bytes.write(buf[i]);
        }
    }
}
