/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import solarcar.vdcListener.VDCConn;

/**
 *
 * @author Ian Pudney
 */
public class VDCmon {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        printVersion();
        Integer portnum=null;
        for(String arg:args)
        {
            if(arg.startsWith("port="))
            {
                portnum=new Integer(arg.substring(5));
            }
        }
        VDCConn.start(portnum);
        segID.initSQLConn();
        while(true)
        {
            String message=br.readLine();
            if(!handleSpecial(message))
            {
                parseMessage p = new parseMessage();
                p.init(message);
            }
        }
    }
    
    static boolean handleSpecial(String message)
    {
        if(message.startsWith("#getVersion"))
        {
            printVersion();
        }
        else return false;
        return true;
    }
    static void printVersion()
    {
        System.out.println("#version 1.3");
    }
    
    
    
}
