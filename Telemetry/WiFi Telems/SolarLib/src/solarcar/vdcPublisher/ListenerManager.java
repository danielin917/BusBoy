package solarcar.vdcPublisher;


import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import solarcar.vdcPublisher.interfaces.SendToBlueHandler;

public class ListenerManager {

    private static final long TIMEOUT_MS = 5000;  //timeout for chat
    private ConcurrentHashMap<String, Listener> listeners;
    private LinkedList<Listener> staticListeners;
    private static ListenerManager lm = null;
    private Thread pruneThread = null;
    private Thread sendThread = null;
    private Thread sendToBlueThread = null;
    private Thread sendBlueChatThread = null;
    private BlockingQueue<String> queue;
    private BlockingQueue<DatagramPacket> sendToBlueQueue;
    private BlockingQueue<String> blueChatQueue;
    private int chatAckLength;
    private int chatOk;
    private SendToBlueHandler sendToBlueHandler;

    private ListenerManager() {
        listeners = new ConcurrentHashMap<>();
        staticListeners = new LinkedList<>();
        queue = new LinkedBlockingQueue<>();
        sendToBlueQueue = new LinkedBlockingQueue<>();
        blueChatQueue = new LinkedBlockingQueue<>();

        sendToBlueHandler = new DummySendToBlueHandler();
        
        //This is a really really jank way of starting multiple threads with access to the same data, notified synchronously
        //Ye from the future, if you come up with a better way of doing this, GO FOR IT.  =]
        
        //Inner classes per job? LinkedBlockingQueue's take() blocks until queue has data,
        //no need for notify()

        chatAckLength = -1;
        chatOk = -1;
    }

    public static ListenerManager get() {
        if (lm == null) {
            lm = new ListenerManager();
            lm.run();
        }
        return lm;
    }

    // broken
    public void addStaticListeners(String[] ips) {
        for (int i = 0; i < ips.length; i++) {
            //staticListeners.add(new Listener(ips[i], 9001));
        }
    }

    public void pruneListeners() {
        System.out.println("Pruning listeners; Current size: " + listeners.size());
        for (Enumeration<String> e = getKeys(); e.hasMoreElements();) {
            String addr = e.nextElement();
            if (listeners.get(addr).age() > 10000) {
                System.out.println("Removing: " + addr + " with age: " + listeners.get(addr).age());
                listeners.remove(addr);
            }
        }
    }
    public void add(Listener l) {
        String key = l.getAddr() + ":" + l.getPort();
        listeners.put(key, l);
    }
    
    public void add(InetAddress addr, int port) {
        String key = addr.getHostAddress() + ":" + port;
        if (listeners.containsKey(key)) {
            listeners.get(key).updateTimestamp();
        } else {
            Listener l = new Listener(addr, port);
            listeners.put(key, l);
        }
    }

    public void add(DatagramPacket p) {
        add(p.getAddress(), p.getPort());
    }
 
    public void add(String addr, int port) {
        InetAddress ad;
        try {
            ad = InetAddress.getByName(addr);
            add(ad, port);
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    // returns Enumeration<Listener> containing all values
    public Enumeration<Listener> getListeners() {
        return listeners.elements();
    }

    public Enumeration<String> getKeys() {
        return listeners.keys();
    }

    public void sendAll(String data) {
        try {
            queue.put(data);

            if (data.indexOf("chatAck") != 0) {
            } else if (data.indexOf("chatOk") != 0) {
            }
            //System.out.println("sendAll: " + data + " put to queue");
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void SendBlue(byte[] buf) {
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.1.4"), 5103); 
            sendToBlueQueue.put(packet);
            System.out.println("SendBlue: Packet added to queue");
        } catch (UnknownHostException | InterruptedException e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void sendBlueChat(String data) {
        try {
            blueChatQueue.put(data);
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }
           
    public void setSendToBlueHandler(SendToBlueHandler handler) {
        this.sendToBlueHandler = handler;
    }

    public void run() {
        pruneThread = new Thread(new PruneJob());
        sendThread = new Thread(new SendJob());
        sendToBlueThread = new Thread(new SendToBlueJob());
        sendBlueChatThread = new Thread(new SendBlueChatJob());

        pruneThread.start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
        
        sendThread.start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
        
        sendToBlueThread.start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
            Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
        }
        
        sendBlueChatThread.start();
    }

// ---------------------- INNER CLASSES ---------------------------------
    private class PruneJob implements Runnable {

        @Override
        public void run() {
            System.out.println("Pruning thread started");
            while (true) {
                try {
                    Thread.sleep(15000);
                    pruneListeners();
                } catch (Exception e) {
                    Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    private class SendJob implements Runnable {

        @Override
        public void run() {
            System.out.println("ListenerManager send thread started");
            while (true) {
                try {
                    String data = queue.take();
                    //System.out.println("sendLoop: sending " + data);
                    for (Enumeration<Listener> e = getListeners(); e.hasMoreElements();) {
                        e.nextElement().send(data);
                    }
                    for (int i = 0; i < staticListeners.size(); i++) {
                        staticListeners.get(i).send(data);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
    
    // TODO: this should probably be refactored out to BlueConn
    private class SendToBlueJob implements Runnable {
        @Override
        public void run() {
            System.out.println("Send to Blue thread started");
            while (true) {
                try {
                    DatagramPacket data = sendToBlueQueue.take();
                    try {
                        System.out.println("sendBlueLoop: Attempting to send packet");
                        sendToBlueHandler.send(data);
                        System.out.println("sendBlueLoop: Packet sent");
                    } catch (Exception e) {
                        System.out.println("sendBlueLoop: Send failed");
                        Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class SendBlueChatJob implements Runnable {

        @Override
        public void run() {
            System.out.println("Chat protocol thread started");
            while (true) {
                try {
                    String data = blueChatQueue.take();
                    System.out.println("received: "+data);
                    try {
                    } catch (Exception e) {
                        System.out.println("Send failed");
                        Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, e);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(ListenerManager.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
