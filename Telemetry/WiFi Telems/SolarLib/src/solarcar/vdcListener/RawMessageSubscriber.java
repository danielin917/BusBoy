package solarcar.vdcListener;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aaresh
 */
public interface RawMessageSubscriber {

    public void parseRawMessage(String str);
}
