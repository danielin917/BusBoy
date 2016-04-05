package solarcar.gui.tabs;


import solarcar.gui.modules.SpeedPane;
import solarcar.gui.modules.PowerPane;
import java.awt.Dimension;
import java.awt.FlowLayout;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.TelemetryGUI;

public class  TopPanel extends SolarArray {

    @Override
    public void createPanels() {
        setPreferredSize(new Dimension(TelemetryGUI.WindowWidth, TelemetryGUI.TopPaneHeight));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        add(new PowerPane());
        add(new SpeedPane());
    }

	public static String getTabName() {
		return "Top Panel";
	}
	
	@Override
	public String toString() {
		return "Top Panel";
	}

}