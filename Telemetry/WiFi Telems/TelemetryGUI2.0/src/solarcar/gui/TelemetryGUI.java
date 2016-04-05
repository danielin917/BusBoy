package solarcar.gui;

import solarcar.vdcListener.VDCConn;
import solarcar.gui.tabs.ChargeTab;
import solarcar.gui.tabs.ReliabilityTab;
import solarcar.gui.tabs.BatteryTab;
import solarcar.gui.tabs.MPPTTab;
import solarcar.gui.tabs.MotorControllerTab;
import solarcar.gui.tabs.ChatTab;
import solarcar.gui.tabs.MotorTab;
import solarcar.gui.tabs.OptionsTab;
import solarcar.gui.tabs.TopPanel;
import solarcar.gui.tabs.BottomOfPackTab;
import solarcar.gui.tabs.ModuleVoltTempTab;
import solarcar.gui.tabs.StrategyTab;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import solarcar.gui.guiElements.FlashingTabbedPane;
import solarcar.gui.guiElements.SolarArray;
import solarcar.gui.guiElements.SolarPanel;
import javax.net.ssl.SSLSocket;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import solarcar.gui.guiElements.SplashScreenDrawer;

public class TelemetryGUI {
    static Integer portnum;
    /**
     * TelemetryGUI function; entry point for the program; starts a new thread with the
     * GUI
     */
    public static void main(String args[]) throws ClassNotFoundException, InstantiationException, InstantiationException, IllegalAccessException, IllegalAccessException, UnsupportedLookAndFeelException {
            //check for specific portnum
            for(String arg:args)
            {
                if(arg.startsWith("port="))
                {
                    portnum=new Integer(arg.substring(5));
                }
            }

            // Create thread to create and show the GUI
            SplashScreenDrawer.get().splashText("");
        
               // Attempt to initialize database connection in a new thread
            SplashScreenDrawer.get().splashText("Attempting to open database connection");
            System.out.println("Attempting to open database connection");
            Thread sqlThread = new Thread("SQL conn") {

                @Override
                public void run() {
                    initSQLConn();
                }
            };
            sqlThread.start(); 
                
                
            try {
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            detectOS();
            
            //***************************
            //Trying to make sure that the TelemGUI will always write the file, even if the logfiles directory doesn't exist yet
            File file;
            if(isWindows())
                file = new File("logfiles\\log_"+System.currentTimeMillis()+".txt");
            else
                file = new File("logfiles/log_"+System.currentTimeMillis()+".txt");
            
            file.getParentFile().mkdirs(); //Make sure the directory exists
            
            logger = new PrintWriter(file);
            
            //*****************************
            /*if(isWindows())
                logger = new PrintWriter(new FileWriter("logfiles\\log_" + System.currentTimeMillis() + ".txt"));
            else
                logger = new PrintWriter(new FileWriter("logfiles/log_" + System.currentTimeMillis() + ".txt"));
            */
            //****************************
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		modulesInArgs = new ArrayList<>();
		for(int i = 0 ; i < args.length; i++) {
			switch (args[i]) {
				case "-r":
					modulesInArgs.add(args[++i]);
					break;
				case "-s":
					showModulesInArgs = true;
					break;
				case "-t":
					topOnly = true;
					break;
				case "-b":
					bottomOnly = true;
					break;
			}
		}

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
				//System.setOut(SplashScreenDrawer.get());
                createAndShowGUI();
            }
        });
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
//		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    }
    
    private static SSLSocket socket;
    private static BufferedReader in = null;
    private static FlashingTabbedPane tabPane;
    private static SolarArray topTab;
    private static Connection conn;
	
	private static ArrayList<String> modulesInArgs;
	private static boolean showModulesInArgs = false;
	private static boolean topOnly = false;
	private static boolean bottomOnly = false;
	
    private enum OS {
        WINDOWS, LINUX, UNIX, MAC
    }
    private static OS os;
    private static String latestPacket;
    private static JFrame frame;
    public static final int WindowWidth = 1024;
    public static int WindowHeight = 695;
    public static final int TopPaneHeight = 280;
    public static final int TabPaneHeight = WindowHeight - TopPaneHeight + 10;
    public static final Dimension HalfGraph = new Dimension(500, 350);
    public static final Dimension QuarterGraph = new Dimension(250, 170);
    public static final Dimension WideGraph = new Dimension(500, 170);
    public static String db = "weather";
    public static final int NUM_BATT_MODULES = 43;
    public static PrintWriter logger;
    public static final PrintStream defaultOut = System.out;

    protected static void populateTabPane() {
        //if(showModulesInArgs == modulesInArgs.contains("WeatherTab"))           tabPane.add("Weather", new WeatherTab());
        if(showModulesInArgs == modulesInArgs.contains("StrategyTab"))			tabPane.add("Strategy", new StrategyTab());
        if(showModulesInArgs == modulesInArgs.contains("ChargeTab"))			tabPane.add("Charge", new ChargeTab());
        if(showModulesInArgs == modulesInArgs.contains("BatteryTab"))			tabPane.add("Battery", new BatteryTab());
        if(showModulesInArgs == modulesInArgs.contains("BottomOfPackTab"))		tabPane.add("Bottom of Pack", new BottomOfPackTab());
        if(showModulesInArgs == modulesInArgs.contains("MotorTab"))			tabPane.add("Motor", new MotorTab());
        if(showModulesInArgs == modulesInArgs.contains("MotorController"))		tabPane.add("Motor Controller", new MotorControllerTab());
        if(showModulesInArgs == modulesInArgs.contains("ModuleVoltTempTab"))    	tabPane.add("Mod Volt/Temp", new ModuleVoltTempTab());
        if(showModulesInArgs == modulesInArgs.contains("MPPTTab"))			tabPane.add("MPPT", new MPPTTab());
        if(showModulesInArgs == modulesInArgs.contains("ReliabilityTab"))		tabPane.add("Reliability", new ReliabilityTab());
        if(showModulesInArgs == modulesInArgs.contains("ChatTab"))			tabPane.add("Chat", new ChatTab());
        tabPane.add("Options", OptionsTab.get());
    }

    public static void initSQLConn() {
        try {
            // Attempt to get a connection to the database
            //	if(conn != null)	s
            //		conn.close();
            conn = null;
            System.out.println("Attempting to connect to sql database");
     //     conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "solar");//localhost
            conn = DriverManager.getConnection("jdbc:mysql://192.168.1.10:3306/mysql", "root", "solar");//VDC

            System.out.println("SQL Connection: " + conn);
            if (conn != null) {
                System.out.println("Connected to sql database");
            } else {
                System.out.println("Claims to be connected, but conn == null");
            }
        } catch (Exception e) {
            // catch an exception on failure to connect; print it out
            System.out.println("Message: " + e.getMessage());
            System.out.println("Connection failed");
        }
    }

    // Allows modules access to the TelemetryGUI frame
    public static JFrame getFrame() {
        return frame;
    }

    // Allows modules access to the sql database connection
    public static Connection getSQLConn() {
        return conn;
    }

    // Detects the OS and stores it
    private static void detectOS() {
        // Get the string containing the OS name; then determine what it is
        String osstr = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (osstr.indexOf("wind") >= 0) {
            os = OS.WINDOWS;
        } else if (osstr.indexOf("nux") >= 0) {
            os = OS.LINUX;
        } else if (osstr.indexOf("mac") >= 0) {
            os = OS.MAC;
        } else if (osstr.indexOf("nix") >= 0) {
            os = OS.UNIX;
        }
    }

    // These four functions tell you what OS you're using; only valid of detectOS() called first
    public static boolean isWindows() {
        return (os == OS.WINDOWS);
    }

    public static boolean isLinux() {
        return (os == OS.LINUX);
    }

    public static boolean isMac() {
        return (os == OS.MAC);
    }

    public static boolean isUnix() {
        return (os == OS.UNIX);
    }

    public static SSLSocket getSocket() {
        return socket;
    }
	
	public static String fileSep() {
		if(isWindows())
			return "\\";
		return "/";
	}

    private static void createAndShowGUI() {
		// Detect OS
		SplashScreenDrawer.get().splashText("Detecting OS");
        System.out.println("detecting OS");
        detectOS();
        
        // Gives the VDCConn the socket to use
		/*SplashScreenDrawer.get().splashText("Setting socket to use");
        System.out.println("setting socket to use");
        VDCConn.get().setSocket(socket);
		*/
		
        SplashScreenDrawer.get().splashText("Creating VDC connection");
	VDCConn.start(logger, portnum,"TelemetryGUI");

//		UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Arial", Font.PLAIN, 10));
		
        // create the TelemetryGUI frame
	SplashScreenDrawer.get().splashText("Creating frame");
        System.out.println("creating frame");
        frame = new JFrame("Aurum WiFi Telemetry");
        frame.setLayout(new FlowLayout());						// set the frame layout
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	// set the frame to exit the program when closed

        // Attempt to set the icon
	SplashScreenDrawer.get().splashText("Attempting to set icon");
        System.out.println("attempting to set icon");
        try {
            frame.setIconImage(ImageIO.read(new File("resources/icon.gif")));	// set the icon
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the top pane 
		if(!bottomOnly) {
			SplashScreenDrawer.get().splashText("Creating top pane");
			System.out.println("creating top pane");
			topTab = new TopPanel();
			topTab.init();
		}

        // Create the tab pane & set it's size
		if(!topOnly) {
			SplashScreenDrawer.get().splashText("Creating tab pane");
			System.out.println("creating tab pane");
			tabPane = FlashingTabbedPane.get();
			tabPane.setPreferredSize(new Dimension(WindowWidth, TabPaneHeight));
			tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

			SplashScreenDrawer.get().splashText("Populating tabs");
			System.out.println("populating tabs");
			populateTabPane();

			for (Component comp : tabPane.getComponents()) {
				if (comp instanceof SolarPanel) {
					((SolarPanel) comp).init();
				}
			}
		}

        // add panes to TelemetryGUI frame
		SplashScreenDrawer.get().splashText("Adding panes to main frame");
        System.out.println("Adding panes to main frame");
        if(!bottomOnly)	frame.add(topTab);
        if(!topOnly)	frame.add(tabPane);
        // pack the frame
		SplashScreenDrawer.get().splashText("Packing frames");
        System.out.println("packing frames");
        frame.pack();

		
        // Set the minimum size and the actual size
		SplashScreenDrawer.get().splashText("Setting minimum size");
		if(bottomOnly)	WindowHeight -= 290;
		if(topOnly)		WindowHeight -= 400;	
		
		System.out.println("setting minimum size");
		
        frame.setMinimumSize(new Dimension(WindowWidth, WindowHeight));

        // Actual size is viewing area plus some extra padding for bars and borders which are OS specific
        SplashScreenDrawer.get().splashText("Setting window size");
		System.out.println("setting window size");
        if (isLinux()) {
            frame.setSize(new Dimension(WindowWidth + 2, WindowHeight + 28));
        } else if (isWindows()) {
            frame.setSize(new Dimension(WindowWidth + 16, WindowHeight + 38));
        } else {
            frame.setSize(new Dimension(WindowWidth, WindowHeight));
        }
        
        // make the frame visible
		SplashScreenDrawer.get().splashText("Making frame visible");
        System.out.println("making frame visible");
        frame.setVisible(true);
        System.out.println("Initialization finished");
		System.setOut(defaultOut);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ex) {
			Logger.getLogger(TelemetryGUI.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		System.out.println("Font: " + (new JLabel()).getFont().getFontName());
		System.out.println("Size: " + (new JLabel()).getFont().getSize());
		
//		MessageHandler.get().newDataMessage(new SolarDataMessage("data id='bps0'"));
    }
}