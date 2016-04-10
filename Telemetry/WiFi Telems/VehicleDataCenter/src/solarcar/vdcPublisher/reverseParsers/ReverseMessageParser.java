package solarcar.vdcPublisher.reverseParsers;

import solarcar.vdc.ReverseCANMessage;
import solarcar.vdc.parsers.DataParser;


public class ReverseMessageParser {

    public final String addr;
    public final DataParser[] subParsers;
	boolean rtr;

    public ReverseMessageParser(DataParser[] parsers, String addr, boolean rtr) {
        this.subParsers = parsers;
        this.addr = addr;
		this.rtr = rtr;
    }

    public String Parse(ReverseCANMessage msg) {
        String str = "can addr='" + addr + "' data='0x0' len='0' priority='0' ext='0' rem='" + (this.rtr ? 1 : 0) + "' ";
		//String from GUI looks like: can addr='0x987' data='0x0123456789ABCDEF' len='8' priority='0' ext='0' rem='0'
        /*for (DataParser p : subParsers) {
            str += p.parse(msg) + " ";
        }*/
        return str;
    }
}