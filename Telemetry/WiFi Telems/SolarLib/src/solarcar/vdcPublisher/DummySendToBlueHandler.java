/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package solarcar.vdcPublisher;

import java.net.DatagramPacket;
import solarcar.vdcPublisher.interfaces.SendToBlueHandler;

/**
 *
 * @author aareshb
 */
public class DummySendToBlueHandler implements SendToBlueHandler {

    @Override
    public void send(DatagramPacket data) {
        
    }
    
}
