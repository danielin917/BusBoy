package solarcar.vdc;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import solarcar.vdcPublisher.ListenerManager;
import solarcar.vdcPublisher.interfaces.SendToBlueHandler;

public class BlueConn implements Runnable, SendToBlueHandler {

    public static final int BLUE_PORT = 5103;
    public static DatagramSocket socket;
    private Thread thread;
    private boolean started = false;
    private static BlueConn cl = null;
    private CharacterBuffer charBuffer;

    private BlueConn() {
        try {
            socket = new DatagramSocket(BLUE_PORT);
        } catch (SocketException ex) {
            Logger.getLogger(BlueConn.class.getName()).log(Level.SEVERE, null, ex); 
        }
        charBuffer = CharacterBuffer.get();
        charBuffer.start();
    }

    public static BlueConn get() {
        if (cl == null) {
            cl = new BlueConn();
            cl.start();
            ListenerManager.get().setSendToBlueHandler(cl);
        }
        return cl;
    }
    
    private void start() {
        if (!started) {
            thread = new Thread(this);
            thread.start();
            started = true;
        }
    }

    @Override
    public void run() {
        int count = 0;
        long time = System.currentTimeMillis();
        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println("BlueConn receive thread started");
        while (true) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                charBuffer.addData(packet.getData(), packet.getOffset(), packet.getLength());
                //String recieved = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(recieved);
                count += packet.getLength();
                if (count > 5000) {
                    System.out.println("Recieving data @ " + 1000 * count /
                            (System.currentTimeMillis() - time+1) + " B/s");
                    time = System.currentTimeMillis();
                    count = 0;
                }
            } catch (IOException | InterruptedException e) {
                Logger.getLogger(BlueConn.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public static DatagramSocket getSocket() {
        return socket;
    }

    @Override
    public void send(DatagramPacket data) throws IOException{
        socket.send(data);
    }
}