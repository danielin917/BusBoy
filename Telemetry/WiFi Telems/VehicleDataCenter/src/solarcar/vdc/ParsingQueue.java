package solarcar.vdc;


import solarcar.vdc.parsers.MessageParser;
import solarcar.vdcPublisher.ListenerManager;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class ParsingQueue {

    private static final String CANSTRUCTURE_FNM = "canstructure.xml";
    private CANStructureParser csp;
    private Worker[] workers;
    private int nThreads;
    private LinkedList<CANMessage> queue;

    public ParsingQueue(int nThreads) {
        this.nThreads = nThreads;
        workers = new Worker[nThreads];
        queue = new LinkedList<CANMessage>();

        csp = new CANStructureParser(CANSTRUCTURE_FNM);
        csp.parse();
    }

    public void start() {
        for (int i = 0; i < nThreads; i++) {
            workers[i] = new Worker();
            workers[i].start();
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addMsgToParse(CANMessage r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class Worker extends Thread {

        public void run() {
			HashSet<Integer> invalidIds = new HashSet<>();
			
            while (true) {
                synchronized (queue) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }

                    while (!queue.isEmpty()) {
                        CANMessage msg = queue.removeFirst();
                        long lmsg = ByteBuffer.wrap(msg.data, 0, 8).getLong();
                        MessageParser mp = csp.getMap().get(msg.id);
                        if (mp == null) {
							if(invalidIds.contains(msg.id)) continue;
							invalidIds.add(msg.id);
	                        System.out.println("null message parser: invalid id (0x" + Long.toHexString(msg.id) + ")");
                            continue;
                        }
                        //System.out.println(Long.toHexString(lmsg));
                        String parsedMsg = mp.Parse(lmsg);
                        msg = null;  //this signals to GC that this memory is now available
                        ListenerManager.get().sendAll(parsedMsg);
                        //System.out.println(parsedMsg);
                    }
                }
            }
        }
    }
}