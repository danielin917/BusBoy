package solarcar.gui.tabs;


import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.modules.SegID;
import solarcar.gui.modules.AnemometerModule;
import solarcar.gui.modules.AnemometerPanel;
import solarcar.gui.modules.MotorPowerGraph;
import solarcar.gui.modules.PowerInGraph;
import solarcar.gui.modules.SOCGraph;
import solarcar.gui.modules.SolarRadiationGraph;
import solarcar.gui.modules.Statistometer;
import solarcar.gui.modules.WindSpeedGraph;
import solarcar.gui.modules.WindDirGraph;

public class  StrategyTab extends SolarArray {
    //ArrayList<SafeSolarPanel> ssps;
    public PowerInGraph StrategyPowerInGraph= new PowerInGraph();
    public MotorPowerGraph StrategyPowerOutGraph = new MotorPowerGraph("Square",3600,2000);
    public SOCGraph StrategySOCGraph = new SOCGraph("Small",3600);
    public SolarRadiationGraph StrategySolarRadiationGraph = new SolarRadiationGraph();
    public WindSpeedGraph StrategyWindSpeedGraph = new WindSpeedGraph(600,60);
    
    public class LeftPanel extends SolarArray {
        @Override
        public void createPanels()
        {
            setLayout(new FlowLayout());
            add(new SegID());
            add(new Statistometer(StrategyPowerInGraph,StrategyPowerOutGraph,StrategySOCGraph, StrategySolarRadiationGraph, StrategyWindSpeedGraph));
            setPreferredSize(new Dimension(280, 350));
        }
    }
    
    public class CenterPanel extends SolarArray {
        @Override
        public void createPanels()
        {
            setLayout(new FlowLayout());
            add(StrategyPowerInGraph);
            add(StrategyPowerOutGraph);
            add(StrategySOCGraph);
            add(StrategySolarRadiationGraph);
            this.setPreferredSize(new Dimension(510, 350));
        }
    }
    
    public class RightPanel extends SolarArray {
        @Override
        public void createPanels()
        {
            setLayout(new FlowLayout());
            add(new AnemometerModule());
            this.setPreferredSize(new Dimension(210, 350));
            add(StrategyWindSpeedGraph);
        }
    }
    
    @Override
    public void createPanels() {
        setLayout(new FlowLayout());

//        add(new WaveMotVoltCurVec());
//        add(new MotorDetailPanel());
        //add(new AnemometerModule());
        //add(new SegID());
        //add(new WindSpeedGraph());
        //add(new WindDirGraph());
        LeftPanel p=new LeftPanel();
        CenterPanel q=new CenterPanel();
        RightPanel r=new RightPanel();
        add(p);
        add(q);
        add(r);
        
    }

    public String getTabName() {
        return "Strategy";
    }
    
}