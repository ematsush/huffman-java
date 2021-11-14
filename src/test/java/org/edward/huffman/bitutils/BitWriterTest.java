package org.edward.huffman.bitutils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitWriterTest {

    @Test
    public void writeBits() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        BitWriter writer = new BitWriter(bytes);

        writeNumBits(1024 * 8, true, writer);
        writer.writeBit(true);
        writer.writeBit(true);
        writeNumBits(6, false, writer);
        writer.writeByte((byte) 100);
        writer.writeShort((short) 256);
        writer.flush();

        byte[] result = bytes.toByteArray();

        assertEquals(1028, result.length);
        assertEquals(-1, result[0]);
        assertEquals(3, result[1024]);
        assertEquals(100, result[1025]);
        assertEquals(0, result[1026]);
        assertEquals(1, result[1027]);
    }

    private void writeNumBits(int n, boolean bit, BitWriter writer) throws IOException {
        for (int i = 0; i < n; i++) {
            writer.writeBit(bit);
        }
    }
}