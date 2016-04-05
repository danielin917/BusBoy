package solarcar.vdc.parsers;


import solarcar.vdc.parsers.IntParser;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntBigendianParser extends IntParser {

    public IntBigendianParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    public String parse(long msg) {
        byte[] bytes = ByteBuffer.allocate(size).putLong(msg).array();
        return super.parse(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong());
    }
}