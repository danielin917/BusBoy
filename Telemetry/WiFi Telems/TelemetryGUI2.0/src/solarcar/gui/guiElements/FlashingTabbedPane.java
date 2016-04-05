package solarcar.gui.guiElements;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

public class FlashingTabbedPane extends JTabbedPane {

    private int _tabIndex;
    private Color _background;
    private Color _foreground;
    private Color _savedBackground;
    private Color _savedForeground;
    private Timer timer = new Timer(500, new ActionListener() {

        private boolean on = false;

        @Override
        public void actionPerformed(ActionEvent e) {
            flash(on);
            on = !on;
        }
    });

    public void flash(int tabIndex, Color foreground, Color background) {

        _tabIndex = tabIndex;
        _savedForeground = getForeground();
        _savedBackground = getBackground();
        _foreground = foreground;
        _background = background;
        timer.start();
    }

    private void flash(boolean on) {
        if (on) {
            if (_foreground != null) {
                setForegroundAt(_tabIndex, _foreground);
            }
            if (_background != null) {
                setBackgroundAt(_tabIndex, _background);
            }
        } else {
            if (_savedForeground != null) {
                setForegroundAt(_tabIndex, _savedForeground);
            }
            if (_savedBackground != null) {
                setBackgroundAt(_tabIndex, _savedBackground);
            }
        }
        repaint();
    }

    public void clearFlashing() {
        timer.stop();
        setForegroundAt(_tabIndex, _savedForeground);
        setBackgroundAt(_tabIndex, _savedBackground);
    }

    /*
     * public static void main(String[] args) { JFrame frame = new JFrame();
     * FlashingTabbedPane tabs = new FlashingTabbedPane(); tabs.addTab("ABC",
     * new JLabel("Tab 1")); tabs.addTab("XYZ", new JLabel("Tab 2"));
     * tabs.flash(1, Color.red, Color.yellow); frame.add(tabs); frame.pack();
     * frame.setVisible(true);
	}
     */
    public int getTabIndex() {
        return _tabIndex;
    }
    static private FlashingTabbedPane tab = null;

    static public FlashingTabbedPane get() {
        if (tab == null) {
            tab = new FlashingTabbedPane();
            tab.addChangeListener(new FlashingTabbedPaneChangeAdapter(tab));
        }
        return tab;
    }
}

class FlashingTabbedPaneChangeAdapter implements javax.swing.event.ChangeListener {

    FlashingTabbedPane pane;

    FlashingTabbedPaneChangeAdapter(FlashingTabbedPane pane) {
        this.pane = pane;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        FlashingTabbedPane source = (FlashingTabbedPane) e.getSource();
        if (pane.getTabIndex() == source.getSelectedIndex()) {
            pane.clearFlashing();
        }
    }
}