package solarcar.vdc.parsers;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UIntBigendianParser extends UIntParser {

    public UIntBigendianParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    public String parse(long msg) {
        byte[] bytes = ByteBuffer.allocate(size).putLong(msg).array();
        //System.err.println(Long.toHexString(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong()));
        return super.parse(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong());
    }
}