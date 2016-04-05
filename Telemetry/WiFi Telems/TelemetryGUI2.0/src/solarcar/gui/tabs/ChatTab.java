package solarcar.gui.tabs;


import solarcar.gui.modules.ChatModule;
import solarcar.gui.guiElements.SolarArray;
import java.awt.FlowLayout;

public class  ChatTab extends SolarArray {

    @Override
    public void createPanels() {
        setLayout(new FlowLayout(FlowLayout.LEADING));
        add(new ChatModule());
    }

    public static String getTabName() {
        return "Chat";
    }
}