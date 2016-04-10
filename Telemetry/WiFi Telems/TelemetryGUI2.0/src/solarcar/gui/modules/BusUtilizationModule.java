package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.util.filters.ExponentialFilter;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.util.filters.SolarFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

public class  BusUtilizationModule extends SolarPanel implements DataMessageSubscriber, Runnable {

    JLabel telemsLabel;
    int telems;
    Thread thread;
    DecimalFormat df;
    SolarFilter filter;

    @Override
    public void init() {
        setBorder(new TitledBorder("Bus Utilization"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        telemsLabel = new JLabel("NO");
        telemsLabel.setForeground(Color.RED);
        telemsLabel.setText("NO");
        telemsLabel.setFont(new Font(telemsLabel.getFont().getFontName(),
                telemsLabel.getFont().getStyle(), 20));


        filter = new ExponentialFilter();
        String[] args = {Double.toString(0.2)};
        filter.setParams(args);

        df = new DecimalFormat("0.00");
        add(telemsLabel);
        thread = new Thread(this, "BusUtilizationModule");
        thread.start();

        MessageHandler.get().subscribeAllData(this);
    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        telems++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            filter.addPoint((double) telems * 108 / (500000 * 0.1) * 100);
            telemsLabel.setText(df.format(filter.filteredPoint()) + "%");
            if (filter.filteredPoint() < 5 || filter.filteredPoint() > 30) {
                telemsLabel.setForeground(Color.RED);
            } else {
                telemsLabel.setForeground(Color.BLACK);
            }
            telems = 0;
        }
    }
}