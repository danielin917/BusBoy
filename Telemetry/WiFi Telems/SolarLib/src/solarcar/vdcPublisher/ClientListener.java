package solarcar.vdcPublisher;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import solarcar.vdcPublisher.interfaces.CmdHandler;

public class ClientListener implements Runnable {

    private Thread thread;
    private static ClientListener cl = null;
    private DatagramSocket socket;
    private CmdHandler cmdHandler;

    private ClientListener() {
        try {
			socket = new DatagramSocket(9999);
        } catch (Exception e) {
            Logger.getLogger(ClientListener.class.getName()).log(Level.SEVERE, null, e);
        }
        cmdHandler = new DummyCmdHandler();
    }

    public static ClientListener get() {
        if (cl == null) {
            cl = new ClientListener();
            cl.start();
        }
        return cl;
    }

    private void start() {
        thread = new Thread(this);
        thread.start();
    }
    
    public void SetCmdHandler(CmdHandler cmdHandler) {
        this.cmdHandler = cmdHandler;
    }

    public byte[] createCanMsg(String received) {
        //This permits sending arbitrary can messages from GUIs - more flexible than cmd
        //String from GUI looks like: can addr='0x987' data='0x0123456789ABCDEF' len='8' priority='0' ext='0' rem='0'

        /*
         * A word of caution: Java is exceedingly stupid regarding bitwise
         * operations Specifically, ALL bitwise operations are performed on
         * ints, and Bytes are signed This means that the implicit promotion
         * from Byte to Int in the bitwise operation screws things up, since the
         * sign is preserved. You need to mask out the 2's complement form that
         * appears as a result. For instance, consider thisoperation: 0
         */
		System.out.println("creating CAN message from string: " + received);
        StringTokenizer st = new StringTokenizer(received, " '");
        st.nextToken(); //"can"
        st.nextToken(); //"addr="
        short addr = Short.parseShort(st.nextToken().substring(2), 16);    //cuts out the "0x"
        st.nextToken(); //"data="
        long data = Long.parseLong(st.nextToken().substring(2).toLowerCase(), 16);    //ditto
        System.out.println("data=" + Long.toHexString(data));
        st.nextToken(); //"len="
        char len = (char) (st.nextToken().charAt(0) - '0');
        st.nextToken(); //"priority="
        char priority = (char) (st.nextToken().charAt(0) - '0');
        st.nextToken(); //"ext="
        char ext = (char) (st.nextToken().charAt(0) - '0');
        st.nextToken(); //"rem="
        char rem = (char) (st.nextToken().charAt(0) - '0');

        byte[] sendBuf = new byte[17];
        //shuffle in addr 8 bits at a time
        for (int x = 0; x < 4; x++) {
            sendBuf[x] = (byte) (addr & 0xff);
            //if(sendBuf[x] < 0) sendBuf[x] = (byte)~(sendBuf[x]&0b01111111);
            addr = (short) (addr >>> 8);
        }

        //data
        //data = data << (8 - len);
        for (int x = 11 - (8 - len); x >= 4; x--) {
            System.out.print(Long.toHexString(data) + " -> ");
            sendBuf[x] = (byte) (data & 0xff);
            //if(sendBuf[x] < 0) sendBuf[x] = (byte)~(sendBuf[x]&0b01111111);
            System.out.print(Long.toHexString(data & 0xff) + " -> " + Integer.toString(sendBuf[x] < 0 ? ~sendBuf[x] + 128 : sendBuf[x], 16));
            data = (long) (data >>> 8);
            System.out.println(" -> " + Long.toHexString(data));
        }

        sendBuf[12] = (byte) len;
        sendBuf[13] = (byte) priority;
        sendBuf[14] = (byte) ((byte) (ext == 1 ? 0b01000000 : 0) + (byte) (rem == 1 ? 0b10000000 : 0));

        byte ck = 0;
        for (int x = 0; x < 15; x++) {
            ck = (byte) (ck ^ sendBuf[x]);
        }

        sendBuf[15] = (byte) Integer.reverse((int) ck << 24);
        sendBuf[16] = '\n';

        /*
         * String hex; for(int x = 0; x < 17; x++) { hex =
         * Integer.toString(sendBuf[x] < 0 ? ~sendBuf[x] + 128: sendBuf[x],16);
         * if(hex.length() < 2) hex = "0"+hex; System.out.print(hex + " ");
         * if(x==3 || x == 11) System.out.print(" "); }
        System.out.println();
         */
        return sendBuf;
    }

    @Override
    public void run() {
        System.out.println("ClientListener receive thread started");
        while (true) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received + " from: " + packet.getAddress().getHostAddress());
            
                if (received.indexOf("beat") == 0) {
                    ListenerManager.get().add(packet);
					//updateTimestamp();
                } else if (received.indexOf("chat-blue") == 0) {
                    ListenerManager.get().sendBlueChat(received);
                } else if (received.indexOf("chat") == 0) {
                    ListenerManager.get().sendAll(received);
                    System.out.println(received);
                } else if (received.indexOf("cmd") == 0) {
                    cmdHandler.handleMessage(received);
                } else if (received.indexOf("can") == 0) {
                    ListenerManager.get().SendBlue(ClientListener.get().createCanMsg(received));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}