package solarcar.vdcSim;


import solarcar.vdcSim.frontEnd.MessageHandler;
import solarcar.vdcSim.frontEnd.VDCConsole;
import solarcar.vdcPublisher.ClientListener;
import solarcar.vdcPublisher.ListenerManager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class VehicleDataCenter {
    
    public static void main(String[] args) {
		MessageHandler mh = MessageHandler.get();
        VDCConsole.get();
		
        ClientListener.get();
		ListenerManager.get().addStaticListeners(args);
    }
}
