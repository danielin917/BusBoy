/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package logplayerlogdiffer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.Vector;
import solarcar.vdcListener.SolarDataMessage;

/**
 *
 * @author aareshb
 */
public class LogPlayerLogDiffer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 2) {
            System.out.print("Need 2 log files to diff. First is original, second is new");
        }
        
        int missingLines = 0; 
        long startOld = 0;
        long startNew = 0;
        long minDiff = Long.MAX_VALUE;
        long maxDiff = Long.MIN_VALUE;
        long diffSum = 0;
        long diffCount = 0;
        
        BufferedReader oldFileReader = Files.newBufferedReader(Paths.get(args[0]));
        BufferedReader newFileReader = Files.newBufferedReader(Paths.get(args[1]));
        BufferedWriter diffsFile = Files.newBufferedWriter(Paths.get("diffs"));
        
        Vector<String> oldFile = new Vector<>();
        Vector<String> newFile = new Vector<>();
        
        String line;
        while((line = oldFileReader.readLine()) != null) {
            if(line.indexOf("data") == 0) {
                oldFile.add(line);
            }
        }

        while((line = newFileReader.readLine()) != null) {
            if(line.indexOf("data") == 0) {
                newFile.add(line);
            }
        }
        
        oldFile.remove(0);
        SolarDataMessage msg = new SolarDataMessage(oldFile.get(0));
        startOld = msg.get("ms").longValue();
        System.out.println("Start time old: " + startOld);
        
        msg = new SolarDataMessage(newFile.get(0));
        startNew = msg.get("ms").longValue();
        System.out.println("Start time new: " + startNew);

        int oldIdx = -1;
        int newIdx = -1;
        
        while(++oldIdx < oldFile.size() && ++newIdx < newFile.size()) {
            boolean quit = false;
            String oldLine = oldFile.get(oldIdx);
            String newLine = newFile.get(newIdx);
            
            int readahead = 1;
            
            while(!compareSubstr(oldLine, newLine) && 
                    (oldIdx + readahead) < oldFile.size() &&
                    (newIdx + readahead) < newFile.size()) {
                if(compareSubstr(oldFile.get(oldIdx + readahead), newLine)) {
                    oldLine = oldFile.get(oldIdx + readahead);
                }
                if(compareSubstr(oldLine, newFile.get(newIdx + readahead))) {
                    newLine = newFile.get(newIdx + readahead);
                }
                
                if(readahead == 4) {
                    break;
                }
                readahead++;
            }
            
            if(readahead == 4) {
                continue;
            }
            
            if(!compareSubstr(oldLine, newLine)) {
                break;
            }
            
            SolarDataMessage oldMsg = new SolarDataMessage(oldLine);
            SolarDataMessage newMsg = new SolarDataMessage(newLine);
            
            long oldDiff = oldMsg.get("ms").longValue() - startOld;
            long newDiff = newMsg.get("ms").longValue() - startNew;
            long diff = (oldDiff - newDiff);
            minDiff = Long.min(minDiff, diff);
            maxDiff = Long.max(maxDiff, diff);
            diffSum += diff;
            diffCount++;
            
            diffsFile.write(diff + "\n");
            
            System.out.println(oldLine + "\toldDiff='" + oldDiff + "'\tdiff='" + diff + "'");
            System.out.println(newLine + "\tnewDiff='" + newDiff + "'");
        }
        
        System.out.println("Missing lines: " + missingLines);
        System.out.println("Max diff: " + maxDiff);
        System.out.println("Min diff: " + minDiff);
        System.out.println("Avg diff: " + (double)diffSum / (double)diffCount);
        diffsFile.close();
        return;

    }
    
    public static boolean compareSubstr(String oldLine, String newLine) {
        String oldSubstr = oldLine.substring(0, oldLine.indexOf("ms="));
        String newSubstr = newLine.substring(0, newLine.indexOf("ms="));  
        return oldSubstr.equals(newSubstr);
    }
    
}
