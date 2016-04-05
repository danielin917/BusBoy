package telemetrylogger2;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Leda Daehler
 */

import telemetrylogger2.TelemetryLogger;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.MessageHandler;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.VDCConn;
import java.util.Vector;


public class dataIn extends TelemetryLogger implements DataMessageSubscriber{
    
    Thread thread;
    public void init() { 
        //subscribe to appropriate canIDs
        for (int i = 0; i < canIDs.size(); ++i)
            MessageHandler.get().subscribeData(canIDs.get(i), this);
        //MessageHandler.get().subscribeAllData(this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage msg) {
        
        //System.out.println("got a message");
        for (int k = 0; k < canIDs.size(); ++k){
            if (msg.getId().equals(canCols.get(k).get(0))){
                for (int l = 1; l < canCols.get(k).size(); ++l){
                    double temp = (msg.get(canCols.get(k).get(l)));
                    rawCanData.get(k).add(temp);
                    
                }
            }
         //   System.out.println(rawCanData.get(k));
        }
    }    
    
    
}
