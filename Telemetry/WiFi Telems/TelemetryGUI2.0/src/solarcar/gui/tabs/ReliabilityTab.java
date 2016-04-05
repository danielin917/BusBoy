package solarcar.gui.tabs;


import solarcar.gui.modules.CruiseCommandModule;
import solarcar.gui.modules.AlarmModule;
import solarcar.gui.modules.CanBusUtilizationGraph;
import solarcar.gui.modules.MotorControllerCommandModule;
import solarcar.gui.modules.MessagesPerSecondGraph;
import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;

public class  ReliabilityTab extends SolarArray {

    @Override
    public void createPanels() {
        String[] dashKeys = {"relaycon","dashbeat"};
        String[] steeringKeys = {"motcmd", "motcmd2", "pttcon", "steeringbeat"};
        String[] bmsMasterKeys = {"bmsvoltextremes", "bmsvoltextremes_aux"};
        String[] motorControllerKeys1 = {"motid", "motflag", "motbus", "motvel", "motphasecurrent", "motvoltvector", "motbackemf", "motrail0", "motrail1", "mottemp0", "mottemp1", "motodo", "motslip"};
        String[] motorControllerKeys2 = {"motid2", "motflag2", "motbus2", "motvel2", "motphasecurrent2", "motvoltvector2", "motbackemf2", "motrail0_2", "motrail1_2", "mottemp0_2", "mottemp1_2", "motodo2", "motslip2"};
        setLayout(new FlowLayout(FlowLayout.LEADING));

        add(new CanBusUtilizationGraph());
        add(new MessagesPerSecondGraph("Dashboard", dashKeys, 5));
        add(new MessagesPerSecondGraph("Steering", steeringKeys, 75));
        add(new MessagesPerSecondGraph("Left Motor Controller", motorControllerKeys1, 75));
        add(new MessagesPerSecondGraph("Right Motor Controller", motorControllerKeys2, 75));
        add(new MessagesPerSecondGraph("BMS Master", bmsMasterKeys, 120));
        add(new AlarmModule());
        //add(new CruiseCommandModule());
        add(new MotorControllerCommandModule());
        System.out.println("mot ctrl cmd module added");
    }
	
	@Override
	public String toString() {
		return "Reliability tab";
	}
}
