package solarcar.vdc.parsers;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FloatBigendianParser extends FloatParser {

    public FloatBigendianParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    @Override
    public String parse(long msg) {
        byte[] bytes = ByteBuffer.allocate(size).putLong(msg).array();
        return super.parse(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong());
    }
}