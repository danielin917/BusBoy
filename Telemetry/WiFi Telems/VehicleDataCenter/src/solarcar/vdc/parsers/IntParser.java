package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


// offset/mult works
public class IntParser extends DataParser {

    public IntParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    @Override
    public String parse(long msg) {
        msg = (msg >>> super.startBit) & super.mask;
        //System.out.println((msg & (long)Math.pow(2, super.size-1)) == 0);
        if ((msg & (long) Math.pow(2, super.size - 1)) == 0) {
            return super.name + "='" + ((msg * super.multiplier) + super.offset) + "'";
        } else {
            return super.name + "='" + (((msg | ~((long) Math.pow(2, super.size - 1) - 1)) * super.multiplier) + super.offset) + "'";
        }
    }
}