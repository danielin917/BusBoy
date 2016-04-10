package solarcar.vdcListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aaresh
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class MessageHandler {

    private LinkedBlockingDeque<SolarDataMessage> dataQueue;
    private LinkedBlockingDeque<SolarChatMessage> chatQueue;
    private LinkedBlockingDeque<ChatMessageSubscriber> chatSubscribers;
    private ConcurrentHashMap<String, LinkedBlockingDeque<DataMessageSubscriber>> dataSubscribers;
    private LinkedBlockingDeque<DataMessageSubscriber> fullDataSubscribers;
    private LinkedBlockingDeque<String> rawQueue;
    private LinkedBlockingDeque<RawMessageSubscriber> rawSubscribers;
    private Thread dataThreadThread[];
    private Thread chatThreadThread[];
	private Thread rawThreadThread[];
	private final DataThread dataThreadObject;
    private final ChatThread chatThreadObject;
	private final RawThread rawThreadObject;
    private static MessageHandler mh = null;
	private static final int nDataThreads = 1;
	private static final int nChatThreads = 1;
	private static final int nRawThreads = 1;
	private static boolean initFinished = false;

    public static MessageHandler get() {
        if (null == mh) {
            mh = new MessageHandler();
			mh.chatQueue.clear();
			for(Thread t : mh.chatThreadThread) {
				t.start();
			}
			mh.dataQueue.clear();
			for(Thread t : mh.dataThreadThread) {
				t.start();
			}
			mh.rawQueue.clear();
			for(Thread t : mh.rawThreadThread) {
				t.start();
			}
			initFinished = true;
        }
        return mh;
    }

    public void subscribeData(String msgId, DataMessageSubscriber sub) {
        if (dataSubscribers.containsKey(msgId)) {
            dataSubscribers.get(msgId).add(sub);
        } else {
            dataSubscribers.put(msgId, new LinkedBlockingDeque<DataMessageSubscriber>());
            dataSubscribers.get(msgId).add(sub);
        }
    }

    public void unsubscribeData(String msgId, DataMessageSubscriber sub) {
        // TODO: implement unsubscribeData
    }

    public void subscribeAllData(DataMessageSubscriber sub) {
        fullDataSubscribers.add(sub);
    }

    public boolean unsubscribeAllData(DataMessageSubscriber sub) {
        // TODO: test
		if(fullDataSubscribers.contains(sub)) {
			return fullDataSubscribers.remove(sub);
		}
		return true;
    }

    public boolean subscribeChat(ChatMessageSubscriber sub) {
        return chatSubscribers.add(sub);
    }

    public boolean unsubscribeChat(ChatMessageSubscriber sub) {
        if (chatSubscribers.contains(sub)) {
            return chatSubscribers.remove(sub);
        }
        return true;
    }
	
	public boolean subscribeRaw(RawMessageSubscriber sub) {
        return rawSubscribers.add(sub);
    }

    public boolean unsubscribeRaw(RawMessageSubscriber sub) {
        if (rawSubscribers.contains(sub)) {
            return rawSubscribers.remove(sub);
        }
        return true;
    }

    public void newDataMessage(SolarDataMessage sdm) {
        dataQueue.offerLast(sdm);
        //System.out.println("Message w/ id " + sdm.getId() + " added to queue");
        synchronized (dataThreadObject) {
            dataThreadObject.notify();
        }
    }

    public void newChatMessage(SolarChatMessage scm) {
        chatQueue.offerLast(scm);
        synchronized (chatThreadObject) {
            chatThreadObject.notify();
        }
    }
	
	public void newRawMessage(String s) {
		rawQueue.offerLast(s);
		synchronized(rawThreadObject) {
			rawThreadObject.notify();
		}
	}

    private MessageHandler() {
        dataQueue = new LinkedBlockingDeque<>();
        chatQueue = new LinkedBlockingDeque<>();
		rawQueue  = new LinkedBlockingDeque<>();
        chatSubscribers = new LinkedBlockingDeque<>();
        dataSubscribers = new ConcurrentHashMap<>();
		rawSubscribers  = new LinkedBlockingDeque<>();
        dataThreadObject = new DataThread();
        chatThreadObject = new ChatThread();
		rawThreadObject  = new RawThread();
		dataThreadThread = new Thread[nDataThreads];
		for(int i = 0; i < nDataThreads; i++) {
			dataThreadThread[i] = new Thread(dataThreadObject, "DataMessageThread" + i);
		}
		chatThreadThread = new Thread[nChatThreads];
		for(int i = 0; i < nChatThreads; i++) {
			chatThreadThread[i] = new Thread(chatThreadObject, "ChatMessageThread" + i);
		}
		rawThreadThread = new Thread[nRawThreads];
		for(int i = 0; i < nRawThreads; i++) {
			rawThreadThread[i] = new Thread(rawThreadObject,  "RawMessageThread" + i);
		}
        fullDataSubscribers = new LinkedBlockingDeque<>();
    }

    private class DataThread implements Runnable {

        SolarDataMessage msg;

        @Override
        public void run() {
            while (true) {
                while (!dataQueue.isEmpty()) {
                    msg = dataQueue.pollLast();
                    //System.out.println("Message w/ id " + msg.getId() + " pulled from queue");
                    if (dataSubscribers.containsKey(msg.getId())) {
                        for (DataMessageSubscriber dsm : dataSubscribers.get(msg.getId())) {
                            //System.out.println("sending to parser: " + dsm.toString());
                            try {
                                dsm.parseDataMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (DataMessageSubscriber dsm : fullDataSubscribers) {
                        //System.out.println("sending to parser: " + dsm.toString());
                        try {
                            dsm.parseDataMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //msg = null;
                }

                synchronized (dataThreadObject) {
                    try {
                        wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ChatThread implements Runnable {

        private SolarChatMessage msg;

        @Override
        public void run() {
            while (true) {
                while (!chatQueue.isEmpty()) {
                    msg = chatQueue.pollLast();
                    for (ChatMessageSubscriber csm : chatSubscribers) {
                        csm.parseChatMessage(msg);
                    }
                }

                synchronized (chatThreadObject) {
                    try {
                        wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

	// TODO: test raw thread/subscriber
	private class RawThread implements Runnable {
        private String msg;

        @Override
        public void run() {
            while (true) {
                while (!rawQueue.isEmpty()) {
                    msg = rawQueue.pollLast();
                    for (RawMessageSubscriber csm : rawSubscribers) {
                        csm.parseRawMessage(msg);
                    }
                }

                synchronized (rawThreadObject) {
                    try {
                        wait();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
