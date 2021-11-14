package org.edward.huffman;

import org.edward.huffman.bitutils.BitReader;
import org.edward.huffman.bitutils.BitWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Huffman {

    public static void main(String[] args) throws IOException {
        if (args.length > 2) {
            switch (args[0]) {
                case "-x":
                    uncompressFile(new File(args[1]), new File(args[2]));
                    break;
                case "-c":
                    compressFile(new File(args[1]), new File(args[2]));
                    break;
                default:
                    throw new IllegalArgumentException("Unkown command " + args[0]);
            }
        } else {
            System.err.println("Insufficient amount of arguments. Provided only " + args.length + " arguments.");
            System.err.println("Usage:");
            System.err.println("-c file_under_compression target_file_name");
            System.err.println("-x file_under_expansion target_file_name");
            System.exit(1);
        }
    }

    private static void compressFile(File file, File targetFile) throws IOException {
        long startTime = System.nanoTime();
        FileInputStream in = new FileInputStream(file);
        Map<Byte, Integer> counts = countBytes(in);
        PriorityQueue<HuffmanNode> deque = constructQueue(counts);
        HuffmanNode tree = constructTree(deque);
        Map<Byte, List<Boolean>> encodings = constructEncodings(tree);
        in.close();

        FileOutputStream compressedFile = new FileOutputStream(targetFile);
        BitWriter writer = new BitWriter(compressedFile);
        writeHeader(encodings, writer);
        in = new FileInputStream(file);
        createCompression(encodings, in, writer);
        in.close();
        compressedFile.close();

        long elapsed = (System.nanoTime() - startTime) / (1000000);
        System.out.println("Completed compression. Time elapsed: " + elapsed);
    }

    private static void uncompressFile(File file, File targetFile) throws IOException {
        long startTime = System.nanoTime();
        FileInputStream fileToUnzip = new FileInputStream(file);
        BitReader reader = new BitReader(fileToUnzip);
        Map<Byte, List<Boolean>> encodings = readHeader(reader);
        HuffmanNode encodingTree = createHuffmanTree(encodings);
        FileOutputStream targetStream = new FileOutputStream(targetFile);
        unzip(encodingTree, reader, targetStream);
        targetStream.close();
        fileToUnzip.close();

        long elapsed = (System.nanoTime() - startTime) / (1000000);
        System.out.println("Completed decompresion. Time elapsed: " + elapsed);
    }

    private static void writeHeader(Map<Byte, List<Boolean>> encodings, BitWriter writer) throws IOException {
        /*
        Header format:
        +-------+
        |       | Header size 2 bytes
        |       |
        +-------+
        |       | Char 1 byte
        +-------+
        |       | Encoding size 1 byte
        +-------+
        |       | Encoding bits determined by previous byte
           ...
        |       |
        +-------+
        |       | File data
           ...
        |       |
        +-------+
         */
        writer.writeShort((short) encodings.size());

        for (Map.Entry<Byte, List<Boolean>> ent : encodings.entrySet()) {
            writer.writeByte(ent.getKey());
            writer.writeByte((byte) ent.getValue().size());
            for (Boolean b : ent.getValue()) {
                writer.writeBit(b);
            }
            int padding = 8 - (ent.getValue().size() % 8);
            for (int i = 0; i < padding; i++) {
                writer.writeBit(false);
            }
        }
    }

    private static Map<Byte, List<Boolean>> readHeader(BitReader reader) throws IOException {
        short headerSize = reader.nextShort();

        Map<Byte, List<Boolean>> encodings = new HashMap<>();

        short encodingsRead = 0;
        while (encodingsRead < headerSize) {
            byte b = reader.nextByte();
            byte encodingSize = reader.nextByte();
            List<Boolean> encoding = new ArrayList<>();
            for (byte i = 0; i < encodingSize; i++) {
                encoding.add(reader.nextBit());
            }
            for (byte i = 0; i < (8 - encodingSize % 8); i++) {
                reader.nextBit();
            }
            encodings.put(b, encoding);
            encodingsRead++;
        }

        return encodings;
    }

    private static void unzip(HuffmanNode root, BitReader reader, OutputStream uncompressedFile) throws IOException {
        HuffmanNode cur = root;
        while (reader.hasNext()) {
            boolean b = reader.nextBit();
            if (b) {
                cur = cur.one;
            } else {
                cur = cur.zero;
            }

            if (cur.one == null && cur.zero == null) {
                uncompressedFile.write(cur.b);
                cur = root;
            }
        }
        uncompressedFile.flush();
    }

    private static HuffmanNode createHuffmanTree(Map<Byte, List<Boolean>> encodings) {
        HuffmanNode root = new HuffmanNode();
        encodings.forEach((k, v) -> {
            HuffmanNode cur = root;
            for (Boolean el : v) {
                if (el) {
                    cur.one = cur.one == null ? new HuffmanNode() : cur.one;
                    cur = cur.one;
                } else {
                    cur.zero = cur.zero == null ? new HuffmanNode() : cur.zero;
                    cur = cur.zero;
                }
            }
            cur.b = k;
        });
        return root;
    }

    private static void createCompression(Map<Byte, List<Boolean>> encodings, InputStream file, BitWriter bWriter) throws IOException {
        int b = file.read();
        while (b != -1) {
            List<Boolean> encoding = encodings.get((byte) b);
            encoding.forEach(el -> {
                try {
                    bWriter.writeBit(el);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            });
            b = file.read();
        }
        bWriter.flush();
    }

    private static Map<Byte, List<Boolean>> constructEncodings(HuffmanNode tree) {
        ArrayDeque<Boolean> deque = new ArrayDeque<>();
        Map<Byte, List<Boolean>> encodings = new HashMap<>();
        constructEncodings(tree, deque, encodings);
        return encodings;
    }

    private static void constructEncodings(HuffmanNode tree, ArrayDeque<Boolean> deque, Map<Byte, List<Boolean>> encodings) {
        if (tree.zero == null && tree.one == null) {
            List<Boolean> encoding = new ArrayList<>();
            deque.descendingIterator().forEachRemaining(encoding::add);
            encodings.put(tree.b, encoding);
        } else {
            deque.addFirst(false);
            constructEncodings(tree.zero, deque, encodings);
            deque.remove();
            deque.addFirst(true);
            constructEncodings(tree.one, deque, encodings);
            deque.remove();
        }
    }

    private static HuffmanNode constructTree(PriorityQueue<HuffmanNode> queue) {
        while (queue.size() > 1) {
            HuffmanNode n1 = queue.poll();
            HuffmanNode n2 = queue.poll();
            HuffmanNode n3 = new HuffmanNode(n1, n2);
            n3.count = n1.count + n2.count;
            queue.add(n3);
        }
        return queue.poll();
    }

    private static PriorityQueue<HuffmanNode> constructQueue(Map<Byte, Integer> counts) {
        PriorityQueue<HuffmanNode> q = new PriorityQueue<>(Comparator.comparingInt(o -> o.count));
        counts.forEach((k, v) -> q.add(new HuffmanNode(k, v)));
        return q;
    }

    private static Map<Byte, Integer> countBytes(InputStream file) throws IOException {
        byte[] buf = new byte[1024];
        int toRead = file.read(buf);

        Map<Byte, Integer> counts = new HashMap<>();

        while (toRead > 0) {
            for (int i = 0; i < toRead; i++) {
                counts.putIfAbsent(buf[i], 0);
                counts.compute(buf[i], (k, v) -> v + 1);
            }
            toRead = file.read(buf);
        }

        return counts;
    }
}
