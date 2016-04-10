package solarcar.vdcSim;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketSender implements Runnable {

    private BlockingQueue<DatagramPacket> queue;
    private static PacketSender conn = null;
    private DatagramSocket socket;
    private Thread sendThread;

    private PacketSender() {
        try {
            queue = new LinkedBlockingQueue<>();
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }


        sendThread = new Thread(this);

        //System.out.println("packet sender constucted");
    }

    private void start() {
        sendThread.start();
    }
    
    public static PacketSender get() {
        if (conn == null) {
            conn = new PacketSender();
            conn.start();
        }
        return conn;
    }

    public void send(String str, InetAddress addr, int port) {
        send(str.getBytes(), addr, port);
    }

    public void send(byte[] buf, InetAddress addr, int port) {
        send(new DatagramPacket(buf, buf.length, addr, port));
    }

    public void send(DatagramPacket packet) {
        try {
            queue.put(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("packet added to queue; notifying");
        synchronized (this) {
            notify();
        }
    }

    @Override
    public void run() {
        System.out.println("send thread started");
        while (true) {
            while (!queue.isEmpty()) {
                DatagramPacket packet = queue.poll();
                try {
                    //System.out.println("Attempting to send packet");
                    socket.send(packet);
                } catch (Exception e) {
                    System.out.println("Send failed");
                    e.printStackTrace();
                }
            }

            //System.out.println("queue is empty; waiting");
            synchronized (this) {
                try {
                    wait();
                } catch (Exception e) {
                    e.printStackTrace();
                    //System.out.println("VDC message: " + e.getMessage());
                }
            }
        }
    }
}
