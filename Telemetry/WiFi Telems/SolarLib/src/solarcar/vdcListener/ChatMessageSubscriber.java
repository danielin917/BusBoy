package solarcar.vdcListener;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aaresh
 */
public interface ChatMessageSubscriber {

    public void parseChatMessage(SolarChatMessage m);
}
