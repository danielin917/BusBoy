package solarcar.vdc;


import java.nio.ByteBuffer;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
public final class CANMessage {

    public int id;
    public byte[] data;
    public long longData;
    public long timestamp;

    public CANMessage(short i, byte[] d) {
        id = i;
        data = d;
        timestamp = System.currentTimeMillis();
    }

    public CANMessage(byte[] buf) {
        //id = (buf[2] << 8) + buf[1];
		/*
         * byte tmp = buf[1]; buf[1] = buf[2]; buf[2] = tmp;
         */

        buf[1] ^= buf[2];
        buf[2] ^= buf[1];
        buf[1] ^= buf[2];

        id = (ByteBuffer.wrap(buf, 1, 2).getShort());
        data = new byte[8];
        for (int x = 3; x < buf.length - 1; x++) {
            data[  (x - 3)] = buf[ buf.length + 1 - x];
            //  3 -> 10
            // 10 ->  3 
            // (12+1)-3 = 10 -> (13)-10 = 3
        }

        timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        String print = "CAN message: id=" + Integer.toString(id < 0 ? ~id + 128 : id, 16) + " \tdata=";
        String hex;
        for (int x = 0; x < data.length; x++) {
            hex = Integer.toString(data[x] < 0 ? ~data[x] + 128 : data[x], 16);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            print = print + hex + " ";
        }
        print += "\ttime=" + timestamp;
        return print;
    }
}
