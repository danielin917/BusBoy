package solarcar.vdcListener;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Random;

public class VDCConn  {

    private ArrayDeque<String> queue;
    private volatile static VDCConn conn = null;
    private Thread sendThreadThread;
	private final SendThread sendThreadObject;
	private Thread receiveThreadThread;
	private final ReceiveThread receiveThreadObject;
//	private PrintWriter out;
//	private SSLSocket socket;
//	private BufferedReader in = null;
    private static String HOST = "192.168.1.10"; // "127.0.0.1";
    private static int PORT = 9999;
    private Integer portnum;
	private boolean initFinished = false;
	private DatagramSocket socket;
	private InetAddress address;
	private PrintStream outputPS;
	private PrintWriter logger;

	
    private VDCConn(PrintStream ssd, Integer portnum) {
        this.portnum=portnum;
        try {
            queue = new ArrayDeque<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		sendThreadObject = new SendThread();
		sendThreadThread = new Thread(this.sendThreadObject, "VDCConn Send thread");
		receiveThreadObject = new ReceiveThread();
		receiveThreadThread = new Thread(this.receiveThreadObject, "VDCConn Recieve thread");

		if(ssd == null) this.outputPS = System.out;
		else			this.outputPS = ssd;
		
        outputPS.println("vdcconn constucted");
    }

	@SuppressWarnings("empty-statement")
    public static VDCConn get() {
        //start();
		// yes, I know this just eats CPU, but it's so short and only during startup, that it's 
		// not worth the effort to do properly, but feel free to fix it
		while(conn != null && !conn.initFinished);
        return conn;
    }
        
	public synchronized static void start() {
		start(null, null, null, null);
	}
	
        public synchronized static void start(PrintStream ssd) {
		start(ssd, null, null, null);
	}
        
	public synchronized static void start(PrintWriter logger) {
		start(null, logger, null, null);
	}
   
        public synchronized static void start(Integer portnum) {
		start(null, null, portnum, null);
	}
        
        public synchronized static void start(String beatSource_in) {
		start(null, null, null, beatSource_in);
	}
        
        public synchronized static void start(PrintStream ssd, PrintWriter logger) {
		start(ssd, logger, null, null);
	}        
        
	public synchronized static void start(PrintStream ssd, Integer portnum) {
		start(ssd, null, portnum, null);
	}        
        
 	public synchronized static void start(PrintStream ssd, String beatSource_in) {
		start(ssd, null, null, beatSource_in);
	}        
        
        public synchronized static void start(PrintWriter logger, Integer portnum) {
		start(null, logger, portnum, null);
	}       
        
        public synchronized static void start(PrintWriter logger, String beatSource_in) {
		start(null, logger, null, beatSource_in);
	}       
       
        public synchronized static void start(Integer portnum, String beatSource_in) {
		start(null, null, portnum, beatSource_in);
	}
	
        public synchronized static void start(PrintWriter logger, Integer portnum, String beatSource_in) {
		start(null, logger, portnum, beatSource_in);
	}
        
        public synchronized static void start(PrintStream ssd, Integer portnum, String beatSource_in) {
		start(ssd, null, portnum, beatSource_in);
	}
        
        public synchronized static void start(PrintStream ssd, PrintWriter logger, String beatSource_in) {
		start(ssd, logger, null, beatSource_in);
	}
	
        public synchronized static void start(PrintStream ssd, PrintWriter logger, Integer portnum) {
		start(ssd, logger, portnum, null);
	}
	
	public synchronized static void start(PrintStream ssd, PrintWriter logger, Integer portnum, String beatSource_in) {		
		if (conn == null) {
            conn = new VDCConn(ssd,portnum);
				
			while( !conn.createSocket(ssd,portnum) );
            conn.outputPS.println("Creating send thread");
			conn.sendThreadThread.start();
			conn.outputPS.println("Creating receive thread");
			conn.receiveThreadThread.start();
			
			// create a new thread to send beats to VDC every 2 second/s
			conn.outputPS.println("Starting beats to VDC");	
			new Thread(new UserBeat(beatSource_in)).start();
			conn.initFinished = true;
			
			conn.logger = logger;
        }
	}
	private boolean createSocket(PrintStream ssd)
        {
            return createSocket(ssd, null);
        }
        
	private boolean createSocket(PrintStream ssd, Integer portnum) {
		outputPS.println("Creating socket");
		
		// Create a new random; 
        if(portnum==null)
        {
            Random rand = new Random();
            portnum=new Integer((rand.nextInt(10000) + 55000));
        }
        // Pick a random port between 55000 and 65000 and attempt to open it
        outputPS.println("attempting to open port");
        try {
            socket = new DatagramSocket(portnum);
            outputPS.println("port " + socket.getPort() + " opened");
			address = InetAddress.getByName(HOST);
        } catch (IOException e) {
            outputPS.println("Failed to open socket");
            return false;
        }
		
		return true;
		
	}
	
/*    public void setSocket(SSLSocket socket) {
        try {
			this.socket = socket;
            out = new PrintWriter(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/

    public void sendMessage(SolarDataMessage sm) {
        sendMessage(sm.getMessage());
    }

    public void sendMessage(String str) {
        queue.addFirst(str);
        synchronized (sendThreadObject) {
			if(address != null)
	            sendThreadObject.notify();
        }
     }

	
	private class SendThread implements Runnable {
		@Override
		public void run() {
			outputPS.println("send thread started");
			while (true) {
				while (!queue.isEmpty()) {
					//outputPS.println("queue isn't empty");
				   String packet = queue.pollFirst();
//					System.out.print("Sending packet to VDC:");
					try {
						socket.send(new DatagramPacket(packet.getBytes(), packet.getBytes().length, address, PORT));
//						outputPS.println(" success");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				//outputPS.println("queue is empty; waiting");
				synchronized (sendThreadObject) {
					try {
						wait();
					} catch (Exception e) {
						e.printStackTrace();
						outputPS.println("VDC message: " + e.getMessage());
					}
				}
			}
		}
	}
	
	private class ReceiveThread implements Runnable {
		@Override
		public void run() {
			outputPS.println("recieve thread started");
			int count = 0;
			int messageCount = 0;
			long time = System.currentTimeMillis();
			
			while (true) {
				String received = "";
                try {
                    // Create buffer and packet
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
                    received = new String(packet.getData(), 0, packet.getLength());
					
//                    System.out.println("VDCConn Recieved packet: " + received);
//                    outputPS.println(received);
                    // if the string is data, process it 
					if(logger != null) {
						logger.println("time='" + Calendar.getInstance().getTime() + "' ms='" + System.currentTimeMillis() + "' " + received);
						logger.flush();
					}
                    switch (received.substring(0, 4)) {
                        case "data":
                            MessageHandler.get().newDataMessage(new SolarDataMessage(received));
                            //outputPS.println("New message id: " + message.getId());
                            break;
                        case "chat":
                            MessageHandler.get().newChatMessage(new SolarChatMessage(received));
                            break;
                        default:
                            break;
                    }
					
                    MessageHandler.get().newRawMessage(received);
					
                    count += received.length();
                    messageCount++;
                    if(count > 5000) {
                    System.out.println("Recieving data @ " + 1000 * count /
                            (System.currentTimeMillis() - time+1) + " B/s, " + 
                            1000 * messageCount / (System.currentTimeMillis() - time+1) 
                            + " msgs/s");
                    time = System.currentTimeMillis();
                    count = 0;
                    messageCount = 0;
                    }

                } catch (IOException e) {
					outputPS.println("IO Exception: ");
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    outputPS.println("Null pointer: ");
                    e.printStackTrace();
                    if(received == null) {
			outputPS.println("\treceived is null, connection to VDC proabably lost");
						
			while( !createSocket(null,portnum) ) {
                            try {
                                outputPS.println("Socket creation failed, trying again in 1 second");
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
				ex.printStackTrace();
                            }
			}
                    }
		}
            }
			
		}
	}
}
