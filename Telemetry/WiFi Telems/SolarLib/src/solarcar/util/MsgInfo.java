/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package solarcar.util;

/**
 *
 * @author Evan
 */
public class MsgInfo implements Comparable{

    public long lastTime;
    public double avgDelta;
    public long N;
    public long numDropouts = 0;
    
    public MsgInfo() {
        lastTime = 0;
        avgDelta = 0;
        N = 0;
        numDropouts = 0;
    }

    @Override
    public int compareTo(Object o) {
        MsgInfo comp = (MsgInfo)o;
        return Long.compare(numDropouts, comp.numDropouts);
    }
}