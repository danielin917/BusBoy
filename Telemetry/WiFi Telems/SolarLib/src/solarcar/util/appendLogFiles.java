package solarcar.util;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Evan
 */
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;

public class appendLogFiles {

    public static void main(String[] args) throws Throwable {
        Scanner scan = new Scanner(System.in);
		String fileSep = "/";
		String osstr = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (osstr.indexOf("wind") >= 0) {
            fileSep = "\\";
        }
        System.out.print("Input folder: ");
        String infolder = scan.next();
        System.out.print("Output filename:  ");
        String outfilename = scan.next();
        long time = System.currentTimeMillis();
        DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS");
        Calendar cal = Calendar.getInstance();
        File folder = new File(infolder);
        File[] files = folder.listFiles();

        PrintWriter out = new PrintWriter(new FileWriter(infolder + fileSep + outfilename));
        for (int x = 0; x < files.length; x++) {
            //if(files[x].isFile() && files[x].lastModified() > time - 3*1000L*60L*60L*12L && files[x].lastModified() < time - 1000L*60L*60L*9L)
            //if (files[x].isFile() && files[x].lastModified() < time - 1000L * 60L * 60L * 24L && files[x].lastModified() > time - 1000L * 60L * 60L * 24L * 2) //if(files[x].lastModified() > time - 1000L*60L*60L*24L)
            if(files[x].isFile())
            {
                cal.setTimeInMillis(files[x].lastModified());
                System.out.println("Appending file " + files[x].getName() + ", last modified " + formatter.format(cal.getTime()));
                BufferedReader in = new BufferedReader(new FileReader(files[x]));
                while (in.ready()) {
                    out.println(in.readLine());
                }
                out.flush();
            }

        }
        out.close();
        System.exit(0);
    }
}
