package solarcar.gui.modules;


import solarcar.vdcListener.MessageHandler;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import solarcar.gui.guiElements.SolarPanel;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.gui.TelemetryGUI;

public class  BoardErrorModule extends SolarPanel implements DataMessageSubscriber {

    JLabel boardLabel;
    int flag = 0;
    int stored_module;

    @Override
    public void init() {
        setBorder(new TitledBorder("Board Error"));
        setPreferredSize(new Dimension(120, 65));
        setLayout(new FlowLayout(FlowLayout.LEADING));
        boardLabel = new JLabel("N/A");
        boardLabel.setForeground(Color.BLACK);
        boardLabel.setText("N/A");
        boardLabel.setFont(new Font(boardLabel.getFont().getFontName(),
                boardLabel.getFont().getStyle(), 20));
        //		boardLabel.setPreferredSize(new Dimension(300,50));
        add(boardLabel);

        for (int i = 0; i < TelemetryGUI.NUM_BATT_MODULES; i++) {
            MessageHandler.get().subscribeData("bps" + i, this);
        }

    }

    @Override
    public void parseDataMessage(SolarDataMessage message) {
        if (flag == 0 && message.get("trouble").intValue() == 1) {
            stored_module = Integer.parseInt(message.getId().substring(3));
            boardLabel.setForeground(Color.RED);
            boardLabel.setFont(new Font(boardLabel.getFont().getFontName(),
                    boardLabel.getFont().getStyle(), 18));
            boardLabel.setText("LOW VOLT(" + stored_module + ")");
            flag = 1;


        } else if (flag == 0 && message.get("trouble").intValue() == 2) {
            stored_module = Integer.parseInt(message.getId().substring(3));
            boardLabel.setForeground(Color.RED);
            boardLabel.setFont(new Font(boardLabel.getFont().getFontName(),
                    boardLabel.getFont().getStyle(), 18));
            boardLabel.setText("NO MESG(" + stored_module + ")");
            flag = 1;

        } else if (flag == 0 && message.get("trouble").intValue() == 0) {
            boardLabel.setForeground(Color.GREEN);
            boardLabel.setText("NORMAL");

        } else if (stored_module == Integer.parseInt(message.getId().substring(3)) && message.get("trouble").intValue() == 1) { //in order to switch between errors
            stored_module = Integer.parseInt(message.getId().substring(3));
            boardLabel.setForeground(Color.RED);
            boardLabel.setFont(new Font(boardLabel.getFont().getFontName(),
                    boardLabel.getFont().getStyle(), 18));
            boardLabel.setText("LOW VOLT(" + stored_module + ")");
            flag = 1;
        } else if (stored_module == Integer.parseInt(message.getId().substring(3)) && message.get("trouble").intValue() == 2) { //in order to switch between errors
            stored_module = Integer.parseInt(message.getId().substring(3));
            boardLabel.setForeground(Color.RED);
            boardLabel.setFont(new Font(boardLabel.getFont().getFontName(),
                    boardLabel.getFont().getStyle(), 18));
            boardLabel.setText("NO MESG(" + stored_module + ")");
            flag = 1;
        } else if (stored_module == Integer.parseInt(message.getId().substring(3)) && message.get("trouble").intValue() == 0) { //only if same board reports okay now
            boardLabel.setForeground(Color.GREEN);
            boardLabel.setText("NORMAL");
            flag = 0;
        }
        //System.out.println(message.get("trouble"));
    }
}