package org.edward.huffman;

class HuffmanNode {
    byte b;
    int count;
    HuffmanNode zero;
    HuffmanNode one;

    HuffmanNode(byte b, int count) {
        this.b = b;
        this.count = count;
    }

    HuffmanNode(HuffmanNode zero, HuffmanNode one) {
        this.zero = zero;
        this.one = one;
    }

    HuffmanNode() {

    }
}
