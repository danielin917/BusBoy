package solarcar.util;


import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class DropoutAnalysis {
    
    public static void main(String[] args) throws Throwable {
        Scanner scan = new Scanner(System.in);
		String fileSep = "/";
		String osstr = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (osstr.indexOf("wind") >= 0) {
            fileSep = "\\";
        }
        System.out.print("Filename:  ");
        String filename = scan.next();
        BufferedReader in = new BufferedReader(new FileReader("logfiles" + fileSep +  filename));
        StringTokenizer st2 = new StringTokenizer(filename, ".");
        String dirname = st2.nextToken();
        DecimalFormat df = new DecimalFormat("0.00");
        HashMap<String, MsgInfo> map = new HashMap<>();

        String line;
        StringTokenizer st;
        while (in.ready()) {
            line = in.readLine();
            try
            {
                st = new StringTokenizer(line, " '=");
                st.nextToken(); //time=
                String time = st.nextToken() + " " + st.nextToken() + " " + st.nextToken() + " " + st.nextToken() + " " + st.nextToken() + " " + st.nextToken();  //time string
                //System.out.println("time: "+time);
                st.nextToken(); //ms=
                long ms = Long.parseLong(st.nextToken());
                //System.out.println("ms: "+ms);
                String type = st.nextToken();
                //System.out.println("type: "+ type);
                if (type.equals("data")) {
                    st.nextToken(); //id=
                    String id = st.nextToken();
                    if (!map.containsKey(id)) {
                        MsgInfo newMsg = new MsgInfo();
                        newMsg.N = 0;
                        newMsg.lastTime = ms;
                        map.put(id, newMsg);
                    } else {
                        MsgInfo cmsg = map.get(id);
                        long cDelta = ms - cmsg.lastTime;
                        cmsg.lastTime = ms;
                        if(cmsg.N > 0)
                        {
                            if(cDelta > cmsg.avgDelta * 1.25)
                            {
                                cmsg.numDropouts++;
                            } else
                            {
                                //only update average if message was not dropout
                                cmsg.avgDelta = (double)(cmsg.avgDelta * cmsg.N + cDelta)/cmsg.N;
                                cmsg.N++;
                            }
                        } else
                        {
                            cmsg.avgDelta = cDelta;
                            cmsg.N++;
                        }
                    }
                }
            } catch(NoSuchElementException e)
            {
                System.err.println("Parsing error - malformed line: " + line);
            }
        }
        Iterator<String> keys = map.keySet().iterator();
        System.out.println("Dropout summary: ");
        while (keys.hasNext()) {
            String cKey = keys.next();
            MsgInfo cmsg = map.get(cKey);
            System.out.println(cKey + "\t\tDropouts: " + cmsg.numDropouts + "\tN: " + cmsg.N + "\t\t avg dT: " + df.format(cmsg.avgDelta));
        }
        System.exit(0);
    }
}
