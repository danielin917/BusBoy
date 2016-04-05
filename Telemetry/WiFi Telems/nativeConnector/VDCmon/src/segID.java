import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ian
 */
public class segID {
    static Connection conn;
     public static void initSQLConn() {
        try {
            // Attempt to get a connection to the database
            //	if(conn != null)	
            //		conn.close();
            conn = null;
            System.out.println("Attempting to connect to sql database");
//           conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "solar");//localhost
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
    public static int acquireCurrentSegment() {
        //System.out.println("attempting to get gps data(segID)");
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
}
