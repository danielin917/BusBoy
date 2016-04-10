/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package telemetrylogger2;

import java.sql.Connection;
import java.sql.DriverManager;
import solarcar.vdcListener.VDCConn;
import java.util.Vector;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Leda Daehler
 */
public class TelemetryLogger{
    static Integer portnum;
    static long rate;
    static Connection conn;
    static Vector<String> canIDs = new Vector<>();
    static Vector<Vector<String> > allCanCols = new Vector<Vector<String>> ();
    static Vector<Vector<String> > canCols = new Vector<Vector<String>> ();
    static Vector<String> tempColNames = new Vector<String>();
    static Vector<Vector<Double>> canData = new Vector<Vector<Double>> ();  
    static Vector<Vector<Double>> rawCanData = new Vector<Vector<Double>> ();
    static Vector<Vector<Double>> blankData = new Vector<Vector<Double>> ();
    static int segID;
        //canData.setSize(canCols.size());
                //each row in canData is a different canID, the data is added
                //as it comes in that rowstatic 
    static int id = -1;

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      //get all filename inputs
        for(String arg:args)
        {
            if(arg.startsWith("port="))
            {
                portnum=new Integer(arg.substring(5));
            }
            else if(arg.startsWith("rate=")){
                rate = new Integer(arg.substring(5));
            }
            else canIDs.add(arg);
        }    
      //read in column names from CAN structure xml in VDC
        System.out.println("Getting CAN structure");

        try{
            File file = new File("..//VehicleDataCenter//canstructure.xml");
 
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                             .newDocumentBuilder();
 
            Document doc = dBuilder.parse(file);
 
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
 
            if (doc.hasChildNodes()) { 
            	printNode(doc.getChildNodes());
                allCanCols.add(tempColNames);
            }
        } 
        catch (Exception e) {
            System.out.println(e.getMessage());
        }        
      //get associated column names from the CAN structure
        
        for(int i = 0; i < canIDs.size(); ++i){
            for (int j = 0; j < allCanCols.size(); ++j){
                if (allCanCols.get(j).get(0).equals(canIDs.get(i))){
                    canCols.add(allCanCols.get(j));
                }
            }
            blankData.add(new Vector<Double>(0));
        }
        
        rawCanData = new Vector<>(blankData);

        Thread msgThread = new Thread("Data In") {
            @Override
            public void run() {      
                VDCConn.start(portnum, "TelemetryLogger");
                dataIn datIn = new dataIn();
                datIn.init();
            }
        };
       msgThread.start();
        
        System.out.println("Attempting to open database connection");        
        Thread sqlThread = new Thread("SQL conn") {
            @Override
            public void run() {      
                initSQLConn();
                dataOut datOut = new dataOut();
                datOut.init();
                segID = getCurrentSegment();
            }
        };
        sqlThread.start(); 
        

    }

    public static void initSQLConn() {
        try {
            // Attempt to get a connection to the database
            //	if(conn != null)	
            //		conn.close();
            conn = null;
            System.out.println("Attempting to connect to sql database");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "solar");//localhost
            //conn = DriverManager.getConnection("jdbc:mysql://192.168.1.10:3306/mysql", "root", "solar");//VDC
            System.out.println("SQL Connection: " + conn);
            if (conn != null) {
                System.out.println("Connected to sql database");
            } else {
                System.out.println("Claims to be connected, but conn == null");
            }
            Statement st = TelemetryLogger.conn.createStatement();
            st.executeUpdate("set time_zone = \'-5:00\';");
        } catch (Exception e) {
            // catch an exception on failure to connect; print it out
            System.out.println("Message: " + e.getMessage());
            System.out.println("Connection failed");
        }
    }

    public static int getCurrentSegment() {
        System.out.println("attempting to get gps data(segID)");
        int segID=-1;
        if(conn==null) return segID;
        if (conn != null) {

            // insert timeout here
            String query = "SELECT * FROM  gps.localcartable ORDER BY recordedtime DESC LIMIT 1";
            Statement st = null;
            try {
                st = conn.createStatement();
                ResultSet rs = st.executeQuery(query);
                if (rs.last()) {
                    segID= rs.getInt("segId");
                }
            } catch (Exception e) {
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                } catch (Exception e) {
                }
            }
        }
        return segID;

    }
    
    private static void printNode(NodeList nodeList) {
        try{
    for (int count = 0; count < nodeList.getLength(); count++) {
	Node tempNode = nodeList.item(count);
	// make sure it's element node.
	if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
		// get node name and value
		if (tempNode.hasAttributes()) {
			// get attributes names and values
			NamedNodeMap nodeMap = tempNode.getAttributes();
 
			for (int i = 0; i < nodeMap.getLength(); i++) {
 
				Node node = nodeMap.item(i);
                            switch (node.getNodeName()) {
                                case "id":
                                    if (tempColNames.size() > 0)  allCanCols.add(tempColNames);
                                    tempColNames = new Vector<>();
                                    tempColNames.add(node.getNodeValue()); 
                                    id++;
                                    break;
                                case "name":
                                    tempColNames.add(node.getNodeValue());
                                    break;
                            }
			}
		}
		if (tempNode.hasChildNodes()) {
			// loop again if has child nodes
			printNode(tempNode.getChildNodes());
		}               
	}
    }
        }
        catch (DOMException e){
        }
  }
 
}
    


