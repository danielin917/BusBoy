package solarcar.gui.tabs;

import java.awt.Color;
import java.awt.Dimension;
import solarcar.gui.modules.MPPTModule;
import solarcar.gui.modules.MPPTTypes;
import solarcar.gui.modules.MPPTPowGraph;
import solarcar.gui.modules.MPPTEffGraph;
import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;

public class MPPTTab extends SolarArray {
    
    MPPTModule mppt1, mppt2, mppt3, mppt4, mppt5;
    
    public class TopPanel extends SolarArray {
        @Override
        public void createPanels()
        {
            setLayout(new FlowLayout());
            mppt1 = new MPPTModule(MPPTTypes.DRIVETEK, 1.5656, Color.red );    //Subarray 1
            add(mppt1);
            mppt2 = new MPPTModule(MPPTTypes.DRIVETEK, 1.627, Color.orange);    //Subarray 2
            add(mppt2);
            mppt3 = new MPPTModule(MPPTTypes.DRIVETEK, 1.648, Color.green);    //Subarray 3
            add(mppt3);
            mppt4 = new MPPTModule(MPPTTypes.DRIVETEK, 1.1594, Color.blue);    //Subarray 4
            add(mppt4);
            mppt5 = new MPPTModule(MPPTTypes.DRIVETEK , .2684*3, Color.magenta);    //Concentrators
            add(mppt5);
            setPreferredSize(new Dimension(1000, 165));
        }
    }
    
    public class BottomPanel extends SolarArray{
        @Override
        public void createPanels()
        {
            setLayout(new FlowLayout());
            add(new MPPTPowGraph(mppt1, mppt2, mppt3, mppt4, mppt5));
            add(new MPPTEffGraph(mppt1, mppt2, mppt3, mppt4, mppt5));

        }
    }
    @Override
    public void createPanels() {
        
        TopPanel p=new TopPanel();
        BottomPanel q=new BottomPanel();
        add(p);
        add(q);
        

    }
}
