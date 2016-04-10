package solarcar.vdcPublisher;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import javax.net.ssl.SSLSocket;

public class Listener  {

    private InetAddress addr;
    private long timestamp;
    private int port;
    private String id;
    
    public Listener(String addr, int port) {
        updateTimestamp();
        try {
            this.addr = InetAddress.getByName(addr);
        } catch (Exception e) {
            //Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, e);
        }
        this.port = port;
    }

    public Listener(InetAddress addr, int port) {
        updateTimestamp();
        this.addr = addr;
        this.port = port;
    }


    public final void updateTimestamp() {
        //yes this needs to be final - don't call overridable methods from own constructor
        timestamp = System.currentTimeMillis();
        //System.out.println("time updated to : " + timestamp);
    }

    public long age() {
        return System.currentTimeMillis() - timestamp;
    }

    public void send(String data) {
        PacketSender.get().send(data, addr, port);
    }
    
    public String getAddr() {
        return addr.getHostAddress();
    }
    
    public int getPort() {
        return port;
    }
}