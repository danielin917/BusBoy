package solarcar.vdc.parsers;

import solarcar.vdc.parsers.DataParser;


public class MessageParser {

    public final String id;
    public final DataParser[] subParsers;

    public MessageParser(DataParser[] parsers, String id) {
        subParsers = parsers;
        this.id = id;
    }

    public String Parse(long msg) {
        String str = "data id='" + id + "' ";
        for (DataParser p : subParsers) {
            str += p.parse(msg) + " ";
        }
        return str;
    }
}