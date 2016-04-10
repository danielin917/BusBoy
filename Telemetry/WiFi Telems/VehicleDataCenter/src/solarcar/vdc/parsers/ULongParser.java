package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


// mult/offset does not work
public class ULongParser extends DataParser {
    /*
     * protected int startBit, mask; protected double multiplier, offset;
     * protected String name;
     */


    public ULongParser(int startBit, int size, double multiplier, double offset, String name) {
        super(startBit, size, multiplier, offset, name);
    }

    @Override
    public String parse(long msg) {
        if (msg < 0) //return super.name + "='" + BigInteger.valueOf(msg).add(BigInteger.valueOf(max_pos)).multiply(BigInteger.valueOf(super.multiplier)).add(BigInteger.valueOf(super.offset)).toString() + "'";
        {
			// TODO: actually parse ULongs
            return "'go to hell java'";
        } else {
            return super.name + "='" + ((msg * super.multiplier) + super.offset) + "'";
        }
    }
}