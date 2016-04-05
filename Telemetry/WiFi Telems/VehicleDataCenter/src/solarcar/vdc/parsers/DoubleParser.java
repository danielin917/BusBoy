package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


// mult/off works
public class DoubleParser extends DataParser {

    public DoubleParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    @Override
    public String parse(long msg) {
        return super.name + "='" + ((Double.longBitsToDouble(msg) * super.multiplier) + super.offset) + "'";
    }
}