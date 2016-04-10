/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package solarcar.logplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcPublisher.ClientListener;
import solarcar.vdcPublisher.ListenerManager;

/**
 *
 * @author aareshb
 */
public class LogPlayer {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.out.println("Need a log file to play");
            return;
        }
        
        String filePath = args[0];
        
        ClientListener.get();
        ListenerManager.get();
        
        new LogPlayer(filePath);
    }
    
    private final AbstractQueue<String> messageQueue;
    private final ListenerManager lm;
    private final BufferedReader file;
    private final Sender sender;
    private final Scheduler scheduler;
    
    private final long realStartTime;
    private final long incrTime = 5 * 1000; // read and place every 5s
    private long endTime;
    private long logStartTime;
    private final ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(3);
    private long correlationId = 0;
    
    public LogPlayer(String filePath) throws IOException {
        messageQueue = new ConcurrentLinkedQueue<>();
        lm = ListenerManager.get();
        file = java.nio.file.Files.newBufferedReader(Paths.get(filePath));
        
        realStartTime = System.currentTimeMillis() + incrTime;
        
        String line;
        while((line = file.readLine()) != null) {
            if(line.indexOf("data") == 0) {
                SolarDataMessage msg = new SolarDataMessage(line);
                logStartTime = msg.get("ms").longValue();
                System.out.println("Start time: " + logStartTime);
                break;
            }
        }
        
        sender = new Sender();
        scheduler = new Scheduler();
        
        endTime = logStartTime + 2*incrTime;
        scheduleExecutor.scheduleAtFixedRate(scheduler, 0, incrTime, TimeUnit.MILLISECONDS);
        
    }
    
    private class Scheduler implements Runnable {

        private int jankCount = 0;
        
        @Override
        public void run() {
            endTime += incrTime;
            String line;
            try {
                System.out.println("Scheduling messages until " + endTime);
                while ((line = file.readLine()) != null) {
                    
                    //if(line.indexOf("data") == 0) {
                    if(line.contains("data")) {
                        //line = line + "corrid='" + (correlationId++) + "'";
                        SolarDataMessage msg = new SolarDataMessage(line);

                        long schedTime = (msg.get("ms").longValue()-logStartTime) - 
                                        (System.currentTimeMillis() - realStartTime);
                        
                        //System.out.println("\tScheduling " + line + "\tTime: " + schedTime);
                        
                        messageQueue.add(line);
                        scheduleExecutor.schedule(sender, schedTime,
                                TimeUnit.MILLISECONDS);
                        
                        
                        if(msg.get("ms") > endTime) {
                            return;
                        }
                    }
                }
                jankCount++;
                if(jankCount > 10) {
                    System.exit(0);
                }
            } catch (IOException ex) {
                Logger.getLogger(LogPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class Sender implements Runnable {

        @Override
        public void run() {
            try {
                String message = messageQueue.remove();
//                System.out.println("Sending: " + message);
                lm.sendAll(message);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }
}
