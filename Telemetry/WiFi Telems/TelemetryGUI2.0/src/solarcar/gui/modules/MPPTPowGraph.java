/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package solarcar.gui.modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.sql.Statement;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.TelemetryGUI;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.util.filters.SolarFilter;
import solarcar.util.graphs.MultiGraphPanel;
import solarcar.vdcListener.DataMessageSubscriber;

/**
 *
 * @author umsolar
 */
public class  MPPTPowGraph extends SolarPanel implements Runnable {
    JLabel taxis;
    JLabel yaxis;
    MultiGraphPanel graph;
    private int updateFreq;

    
    int duration;
    MPPTModule mppt1, mppt2, mppt3, mppt4, mppt5;
    
    Thread thread;

    public MPPTPowGraph(MPPTModule mppt1In, MPPTModule mppt2In, MPPTModule mppt3In, MPPTModule mppt4In, MPPTModule mppt5In){
        mppt1 = mppt1In;
        mppt2 = mppt2In;
        mppt3 = mppt3In;
        mppt4 = mppt4In;
        mppt5 = mppt5In;
        
        setBorder(new TitledBorder("MPPT Power Graph"));
        duration = 600;
        setPreferredSize(TelemetryGUI.QuarterGraph);
        setLayout(new BorderLayout());
        taxis = new JLabel("Time");
        yaxis = new JLabel("<html>P<br/>o<br/>w<br/>e<br/>r</html>");
        graph = new MultiGraphPanel(duration, 20, 20, 400, 0, 5);
        graph.setColor(0, mppt1.labelColor);
        graph.setColor(1, mppt2.labelColor);
        graph.setColor(2, mppt3.labelColor);
        graph.setColor(3, mppt4.labelColor);
        graph.setColor(4, mppt5.labelColor);

        graph.start();

        add(taxis, BorderLayout.SOUTH);
        add(yaxis, BorderLayout.WEST);
        add(graph, BorderLayout.CENTER);

        taxis.setHorizontalAlignment(JLabel.CENTER);
        
        

        updateFreq = 1;
    }
    
    @Override
    public void init() {
        
        thread = new Thread(this, "MPPT Power Graph");
        thread.start();

    }
    
    @Override
    public void run(){
        System.out.println("Trying to get average data");
        Statement st = null;
        while (true) {
            graph.addPoint(0, mppt1.pin);
            graph.addPoint(1, mppt2.pin);
            graph.addPoint(2, mppt3.pin);
            graph.addPoint(3, mppt4.pin);
            graph.addPoint(4, mppt5.pin);

        
         try {
                Thread.sleep(1000 / updateFreq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
