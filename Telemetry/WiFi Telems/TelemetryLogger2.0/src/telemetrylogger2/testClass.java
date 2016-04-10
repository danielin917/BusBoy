/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package telemetrylogger2;

/**
 *
 * @author Leda Daehler
 */
public class testClass {
    Thread thread;
    public void init(){
        
    thread = new Thread();
    thread.start();
    }
    
    public void run(){
        System.out.println("it here!!");
        
    }
}
