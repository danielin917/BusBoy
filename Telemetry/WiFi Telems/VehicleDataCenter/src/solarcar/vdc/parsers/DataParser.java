package solarcar.vdc.parsers;


public abstract class DataParser {

    public final long startBit, mask;
    public final double multiplier, offset;
    public final String name;
    public final int size;
    public final int shift;
    protected long masks[] = {
        0x0L,
        0x1L,
        0x3L,
        0x7L,
        0xfL,
        0x1fL,
        0x3fL,
        0x7fL,
        0xffL,
        0x1ffL,
        0x3ffL,
        0x7ffL,
        0xfffL,
        0x1fffL,
        0x3fffL,
        0x7fffL,
        0xffffL,
        0x1ffffL,
        0x3ffffL,
        0x7ffffL,
        0xfffffL,
        0x1fffffL,
        0x3fffffL,
        0x7fffffL,
        0xffffffL,
        0x1ffffffL,
        0x3ffffffL,
        0x7ffffffL,
        0xfffffffL,
        0x1fffffffL,
        0x3fffffffL,
        0x7fffffffL,
        0xffffffffL,
        0x1ffffffffL,
        0x3ffffffffL,
        0x7ffffffffL,
        0xfffffffffL,
        0x1fffffffffL,
        0x3fffffffffL,
        0x7fffffffffL,
        0xffffffffffL,
        0x1ffffffffffL,
        0x3ffffffffffL,
        0x7ffffffffffL,
        0xfffffffffffL,
        0x1fffffffffffL,
        0x3fffffffffffL,
        0x7fffffffffffL,
        0xffffffffffffL,
        0x1ffffffffffffL,
        0x3ffffffffffffL,
        0x7ffffffffffffL,
        0xfffffffffffffL,
        0x1fffffffffffffL,
        0x3fffffffffffffL,
        0x7fffffffffffffL,
        0xffffffffffffffL,
        0x1ffffffffffffffL,
        0x3ffffffffffffffL,
        0x7ffffffffffffffL,
        0xfffffffffffffffL,
        0x1fffffffffffffffL,
        0x3fffffffffffffffL,
        0x7fffffffffffffffL,
        0xffffffffffffffffL
    };

    public DataParser(int startBit, int size, double multiplier, double offset, String name) {
        this.startBit = startBit;
        this.mask = masks[size]; //(int) Math.pow(2, size) - 1;
        this.multiplier = multiplier;
        this.offset = offset;
        this.name = name;
        this.size = size;
//		this.shift = 64 - startBit - size;
        this.shift = startBit;
    }

    public DataParser(int value, String name) {
        this.name = name + "'" + value + "'";
        this.startBit = 0;
        this.mask = 0;
        this.multiplier = 0;
        this.offset = 0;
        this.size = 0;
        this.shift = 0;
    }

    public abstract String parse(long msg);
}