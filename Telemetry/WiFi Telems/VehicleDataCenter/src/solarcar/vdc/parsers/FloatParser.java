package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


// offset/mult works
public class FloatParser extends DataParser {

    public FloatParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    public String parse(long msg) {
        //System.out.println(super.startBit+"\n"+Long.toHexString(super.mask));
        //System.out.println(Integer.toHexString((int)((msg >> super.startBit) & super.mask)));
        return super.name + "='" + (((Float.intBitsToFloat((int) ((msg >> super.startBit) & super.mask))) * super.multiplier) + super.offset) + "'";
    }
}