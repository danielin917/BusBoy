package solarcar.vdc;


import solarcar.vdcPublisher.ClientListener;
import solarcar.vdcPublisher.ListenerManager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class VehicleDataCenter {
    
    public static void main(String[] args) {
        VDCConsole console = new VDCConsole("Vehicle Data Center");
        console.start();
        
        ClientListener.get();
        BlueConn.get();
        StratConn.get();
	ListenerManager.get().addStaticListeners(args);
    }
}
