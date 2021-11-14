package org.edward.huffman.bitutils;

import java.io.IOException;
import java.io.InputStream;

public class BitReader {

    private final InputStream bytes;
    private final byte[] buf = new byte[1024];
    private int bitOff;
    private int readable;

    public BitReader(InputStream bytes) throws IOException {
        this.bytes = bytes;
        this.bitOff = 0;
        this.readable = bytes.read(buf);
    }

    public boolean nextBit() throws IOException {
        if (!hasNext()) {
            throw new IllegalStateException("No more bits to read.");
        }
        boolean result = getBit();
        if (bitOff / Byte.SIZE == readable) {
            readable = bytes.read(buf);
            bitOff = 0;
        }
        return result;
    }

    public byte nextByte() throws IOException {
        if (!isByteAligned()) {
            throw new IllegalStateException("Trying to read non-aligned byte");
        }

        if (!hasNext()) {
            throw new IllegalStateException("No more bytes to read.");
        }

        byte result = buf[bitOff / Byte.SIZE];
        bitOff += Byte.SIZE;
        if (bitOff / Byte.SIZE == readable) {
            readable = bytes.read(buf);
            bitOff = 0;
        }
        return result;
    }

    public short nextShort() throws IOException {
        if (!isByteAligned()) {
            throw new IllegalStateException("Trying to read non-aligned byte");
        }

        if (!hasNext()) {
            throw new IllegalStateException("No more bytes to read.");
        }

        byte lower = nextByte();
        byte upper = nextByte();

        return (short) ((upper << 8) | (lower & 0xFF));
    }

    public boolean isByteAligned() {
        return bitOff % 8 == 0;
    }

    public boolean hasNext() {
        return readable != -1;
    }

    private boolean getBit() {
        boolean result = 0 < (buf[bitOff / Byte.SIZE] & ((byte) 0x1 << (bitOff % Byte.SIZE)));
        bitOff++;
        return result;
    }
}
