package solarcar.vdc;


import solarcar.vdcPublisher.reverseParsers.ReverseMessageParser;
import solarcar.vdcPublisher.ListenerManager;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import solarcar.vdcPublisher.ClientListener;

public class ReverseParsingQueue {

    private static final String CANSTRUCTURE_FNM = "canstructure.xml";
    private CANStructureParser csp;
    private Worker[] workers;
    private int nThreads;
    private LinkedList<ReverseCANMessage> queue;

    public ReverseParsingQueue(int nThreads) {
        this.nThreads = nThreads;
        workers = new Worker[nThreads];
        queue = new LinkedList<>();

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

    public void addMsgToParse(ReverseCANMessage r) {
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class Worker extends Thread {

		@Override
        public void run() {
		//	HashSet<Integer> invalidIds = new HashSet<>();
			
            while (true) {
                synchronized (queue) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }

                    while (!queue.isEmpty()) {
                        ReverseCANMessage msg = queue.removeFirst();
						//System.out.println("Parsing message: " + msg.getMessage());
                        ReverseMessageParser mp = csp.getReverseMap().get(msg.getId());
                        if (mp == null) {
		//					if(invalidIds.contains(msg.id)) continue;
		//					invalidIds.add(msg.id);
	                        System.out.println("null reverse message parser: invalid id (" + msg.getId() + ")");
                            continue;
                        }
                        //System.out.println(Long.toHexString(lmsg));
                        //String parsedMsg = mp.Parse(msg);
						ListenerManager.get().SendBlue(ClientListener.get().createCanMsg(mp.Parse(msg)));
                        msg = null;  //this signals to GC that this memory is now available
        //                ListenerManager.get().sendAll(parsedMsg);
                        //System.out.println(parsedMsg);
                    }
                }
            }
        }
    }
}