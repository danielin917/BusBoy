package solarcar.vdc;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evan
 */
public class CharacterBuffer implements Runnable {

    private static CharacterBuffer cl;
    private BlockingQueue<Byte> queue;
    private Thread thread;

    private CharacterBuffer() {
        try {
            queue = new LinkedBlockingQueue<>();
        } catch (Exception e) {
            Logger.getLogger(CharacterBuffer.class.getName()).log(Level.SEVERE, null, e);
        }
        thread = new Thread(this, "CharacterBuffer");
    }
    
    public void start() {
        thread.start();
    }

    public static CharacterBuffer get() {
        if (cl == null) {
            cl = new CharacterBuffer();
        }
        return cl;
    }

    public synchronized void addData(byte[] buf, int offset, int len) throws InterruptedException {
        //System.out.println("Adding " + (len) + " bytes of data to queue");
        int count = 0;
        for (int x = offset; x < offset + len; x++) {
            queue.put(buf[x]);
            //System.out.print((buf[x])+"\t");
            count++;
        }
        //System.out.println();
    }

    @Override
    public void run() {
        ParsingQueue pq = new ParsingQueue(20);
        pq.start();
        byte[] buf = new byte[12];
        while (true) {
            for (int x = 0; x < 12; x++) {
                try {
                    buf[x] = queue.take();
                } catch (InterruptedException e) {
                    Logger.getLogger(CharacterBuffer.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            /*
             * for(int x = 0; x < buf.length; x++)
             * System.out.print(Integer.toHexString((int)buf[x]+128)+" ");
            System.out.println();
             */

            if (isValidCanMessage(buf)) {
                CANMessage msg = new CANMessage(buf);
                //System.out.println(msg);
                pq.addMsgToParse(msg);
            } else {
                //skip to next CAN message
                // TODO make can message error handling more ideal in case of malformed CAN
                // Right now it just scans forward until it finds something, possibly dropping the following (valid) message
                System.out.println("Invalid can message, fixing...");
                int count = 0;
                while (!queue.isEmpty() && queue.peek() != -5) //-5 is 0xFB
                {
                    try {
                        queue.take();
                        count++;
                    } catch (InterruptedException e) {
                        Logger.getLogger(CharacterBuffer.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                //System.out.println("Skipped " + count + " bytes");
                //if(queue.isEmpty()) System.out.println("Queue is now empty"); else System.out.println("Data remains in queue");
            }
        }
    }

    public boolean isValidCanMessage(byte[] msg) {
        if (msg[0] != -5) {
            //System.out.println("First character is bad: 123 != " + msg[0]+128);
            return false;
        }
        byte checksum = 0;
        for (int x = 1; x < msg.length - 1; x++) {
            checksum = (byte) (checksum ^ msg[x]);
        }
        return (checksum == msg[msg.length - 1]);
    }
}
