package solarcar.gui.modules;


import java.awt.Dimension;
import java.awt.FlowLayout;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.TelemetryGUI;

public class  SpeedPane extends SolarArray {

    @Override
    public void createPanels() {
        setPreferredSize(new Dimension(((TelemetryGUI.WindowWidth / 4) - 5), TelemetryGUI.TopPaneHeight));
        setLayout(new FlowLayout(FlowLayout.LEADING));

        add(new TorqueModule());
        add(new Speedometer());
        add(new BusUtilizationModule());
        add(new AreAllModulesConnectedModule());
        add(new OverVoltageModule());
        add(new BatteryTempModule());
        add(new RelayStatus());
        add(new BoardErrorModule());
    }
	
	@Override
	public String toString() {
		return "Speed Pane";
	}
}