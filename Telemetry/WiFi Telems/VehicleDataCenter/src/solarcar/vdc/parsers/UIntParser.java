package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


// mult/offset works
public class UIntParser extends DataParser {

    public UIntParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    @Override
    public String parse(long msg) {
        //System.out.println("Hex string: " + Long.toHexString(msg) + " parsing: " + super.name + " value: " + Long.toHexString(((msg >>> super.shift) & super.mask)));
        return super.name + "='" + ((((msg >>> super.shift) & super.mask) * super.multiplier) + super.offset) + "'";
    }
}