package solarcar.util;


import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class LogToCSVSet {

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
        new File("logfiles" + fileSep + dirname).mkdir();

        HashMap<String, PrintWriter> out = new HashMap<>();
        PrintWriter chatOut = new PrintWriter(new FileWriter("logfiles" + fileSep + dirname +  fileSep +"chat.txt"));

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
                String ms = st.nextToken();
                //System.out.println("ms: "+ms);
                String type = st.nextToken();
                //System.out.println("type: "+ type);
                if (type.equals("data")) {
                    st.nextToken(); //id=
                    String id = st.nextToken();
                    if (!out.containsKey(id)) {
                        //this method loses the first data point of each but seems the fastest way to implement
                        //System.out.println("Writing to " + dirname);
                        out.put(id, new PrintWriter(new FileWriter("logfiles" + fileSep + dirname +  fileSep +  id + ".csv")));
                        out.get(id).print("time,ms,");
                        while (st.hasMoreTokens()) {
                            out.get(id).print(st.nextToken() + ",");
                            st.nextToken();
                        }
                        out.get(id).println();
                        out.get(id).flush();
                    } else {
                        out.get(id).print(time + ",");
                        out.get(id).print(ms + ",");
                        while (st.hasMoreTokens()) {
                            st.nextToken();
                            out.get(id).print(st.nextToken() + ",");
                        }
                        out.get(id).println();
                        out.get(id).flush();
                    }
                } else if(type.equals("chat"))
                {
                    //example: 
                    //time='Thu Jun 20 07:05:57 EDT 2013' ms='1371726357770' chat msg='Bueller? ' id='Evan'
                    int msg_start = line.indexOf("msg='") + "msg='".length();
                    int msg_end = line.indexOf("'", msg_start + 1);
                    String message = line.substring(msg_start, msg_end);
                    st.nextToken(); //id=
                    String uid = line.substring(line.indexOf("id='") + "id='".length(),
                line.indexOf("'", line.indexOf("id='") + "id='".length() + 1));
                    
                    chatOut.println(uid + "(" + time + "): " + message);
                    chatOut.flush();
                }
            } catch(NoSuchElementException e)
            {
                System.err.println("Parsing error - malformed line: " + line);
            }
        }
        Iterator<String> keys = out.keySet().iterator();
        while (keys.hasNext()) {
            out.get(keys.next()).close();
        }
        chatOut.close();
        System.exit(0);
    }
}
