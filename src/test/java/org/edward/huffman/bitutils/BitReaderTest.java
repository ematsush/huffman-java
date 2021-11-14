package org.edward.huffman.bitutils;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BitReaderTest {

    @Test
    public void readBits() throws IOException {
        byte[] bytes = {0xA, 0x1, 0x2, 0xA, 0x7, 0x02, 0x01};
        BitReader reader = new BitReader(new ByteArrayInputStream(bytes));

        assertTrue(reader.hasNext());

        assertEquals(0xA, reader.nextByte());

        assertTrue(reader.isByteAligned());

        assertTrue(nextBits(1, true, reader));
        assertTrue(nextBits(7, false, reader));

        assertTrue(reader.isByteAligned());

        assertTrue(nextBits(1, false, reader));
        assertTrue(nextBits(1, true, reader));
        assertTrue(nextBits(6, false, reader));

        assertEquals(0xA, reader.nextByte());

        assertTrue(nextBits(3, true, reader));
        assertTrue(nextBits(5, false, reader));

        assertEquals(258, reader.nextShort());

        assertFalse(reader.hasNext());
    }

    @Test
    public void zeroLength() throws IOException {
        BitReader reader = new BitReader(new ByteArrayInputStream(new byte[0]));

        assertFalse(reader.hasNext());
    }

    @Test
    public void badNextBit() {
        assertThrows(IllegalStateException.class, () -> {
            BitReader reader = new BitReader(new ByteArrayInputStream(new byte[0]));

            reader.nextBit();
        });
    }

    @Test
    public void badNextByte() {
        assertThrows(IllegalStateException.class, () -> {
            BitReader reader = new BitReader(new ByteArrayInputStream(new byte[2]));

            reader.nextBit();
            reader.nextByte();
        });
    }

    public boolean nextBits(int n, boolean bit, BitReader reader) throws IOException {
        boolean result = true;
        for (int i = 0; i < n; i++) {
            result = result && (bit == reader.nextBit());
        }
        return result;
    }
}