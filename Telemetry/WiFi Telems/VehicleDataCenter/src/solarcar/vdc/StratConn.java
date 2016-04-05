package solarcar.vdc;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StratConn implements Runnable {

    public static final int STRAT_PORT = 9001;
    private static DatagramSocket socket;
    private boolean started = false;
    private Thread thread;
    private static StratConn conn = null;
    
    private StratConn() {
        try {
            socket = new DatagramSocket(STRAT_PORT);
        } catch (SocketException ex) {
            Logger.getLogger(StratConn.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        System.out.println("StratConn receive thread started");
        while (true) {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
            } catch (IOException ex) {
                Logger.getLogger(StratConn.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
    public static StratConn get() {
        if (conn == null) {
            conn = new StratConn();
            conn.start();
        }
        return conn;
    }
}
