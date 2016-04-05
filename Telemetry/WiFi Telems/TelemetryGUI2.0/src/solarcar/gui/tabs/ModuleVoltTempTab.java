package solarcar.gui.tabs;


import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;
import solarcar.gui.modules.ModuleTempBarGraphBigger;
import solarcar.gui.modules.ModuleVoltageBarGraph;
import solarcar.gui.TelemetryGUI;

public class  ModuleVoltTempTab extends SolarArray {
    //protected ArrayList<JPanel> jpanels;

    @Override
    public void createPanels() {
        setLayout(new FlowLayout(FlowLayout.LEADING));
        add(new ModuleVoltageBarGraph(TelemetryGUI.HalfGraph));
        add(new ModuleTempBarGraphBigger());
    }

    public static String getTabName() {
        return "Module Volt/Temp";
    }
}
