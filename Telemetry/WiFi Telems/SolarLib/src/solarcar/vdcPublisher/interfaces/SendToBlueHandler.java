/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package solarcar.vdcPublisher.interfaces;

import java.io.IOException;
import java.net.DatagramPacket;

/**
 *
 * @author aareshb
 */
public interface SendToBlueHandler {
    public void send(DatagramPacket data) throws IOException;
}
