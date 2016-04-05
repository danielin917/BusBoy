package solarcar.gui.modules;


import solarcar.gui.guiElements.SolarPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  AnemometerModule extends SolarPanel {

    AnemometerPanel ap;
    JLabel speedLabel;

    @Override
    public void init() {
        setBorder(new TitledBorder("Weather"));
        setPreferredSize(new Dimension(195, 225));
        setLayout(new BorderLayout(5, 5));

        speedLabel = new JLabel("N/A m/s N/A&deg;");
        add(speedLabel, BorderLayout.SOUTH);
        speedLabel.setHorizontalAlignment(JLabel.CENTER);

        ap = new AnemometerPanel(speedLabel);
		ap.start();
        //ap.setPreferredSize(new Dimension(240,240));
        add(ap, BorderLayout.CENTER);
    }
}