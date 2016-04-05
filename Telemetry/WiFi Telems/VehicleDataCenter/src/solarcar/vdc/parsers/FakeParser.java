package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


public class FakeParser extends DataParser {

    public FakeParser(int value, String name) {
        super(value, name);
    }

    @Override
    public String parse(long msg) {
        return name;
    }
}