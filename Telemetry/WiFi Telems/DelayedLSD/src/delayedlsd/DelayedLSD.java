/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delayedlsd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.lang.Object;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Vector;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Delayed Live Simulator Data
 *
 * @author Leda Daehler
 *
 * Reads most recent simulator then sends to another table, delayed, as if the
 * simulator was being ran real time. Needs to be running for the telemetry GUI
 * to display predicted values.
 *
 */
public class DelayedLSD {

    private static String dbIn, dbOut;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //check for specific portnum
        for (String arg : args) {
            if (arg.startsWith("in=")) {
                dbIn = arg.substring(3);
            }
            if (arg.startsWith("out=")) {
                dbOut = arg.substring(4);
            }
        }
        System.out.println("Attempting to open database connection");

        initSQLConn();
        Thread sqlRunThread = new Thread("SQL access") {
            @Override
            public void run() {
                readAndWrite();
            }
        };
        sqlRunThread.start();
    }
    private static Connection conn;

    public static void initSQLConn() {
        try {
            // Attempt to get a connection to the database
            //	if(conn != null)	
            //		conn.close();

            conn = null;
   //         System.out.println("Attempting to connect to sql database");
   //        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mysql", "root", "solar");//localhost
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

    private static void readAndWrite() {

        if (conn != null) {
            System.out.println("Reading from " + dbIn);
            Statement st = null;
            Statement st2 = null;
            long time = 0, curTime, lastTime;
            boolean initialCatchUp_flag = false;
            String line;
            try {
                st = conn.createStatement();
                ResultSet rs2 = st.executeQuery("SELECT max(simID) FROM " + dbIn + ";");
                rs2.next();
                ResultSet rs = st.executeQuery("SELECT * FROM " + dbIn + " where simID=" + rs2.getInt("max(simID)")+ ";");
                ResultSetMetaData rsMetaData = rs.getMetaData();

                //get column names
                int numberOfColumns = rsMetaData.getColumnCount();
                Vector<String> columnNames = new Vector<>(numberOfColumns);
                Deque<String> statements = new LinkedList<String>();
                for (int i = 1; i < numberOfColumns + 1; ++i) {
                    String columnName = rsMetaData.getColumnName(i);
                    columnNames.add(columnName);
                    System.out.println("column name= " + columnName);
                }

                System.out.println("Writing to " + dbOut);
                
	
                while (rs.next()) {
                    lastTime = time;
                    time = rs.getTimestamp("segmentTime").getTime();

                    //System.out.println(time + " " + curTime);
                    if (time > System.currentTimeMillis()) {
                        if (!initialCatchUp_flag) {
                            while (rs.previous()) {
                                String insertLine = makeInsertLine(rs, rsMetaData, columnNames, numberOfColumns);

                                statements.addFirst(insertLine);

                            }
                            st2 = conn.createStatement();
                            while (statements.peekFirst() != null) {
                                line = statements.removeFirst();
                                System.out.println(line);
                                st2.executeUpdate(line);
                            }
                            initialCatchUp_flag = true;
                        } else {
                            while (time > System.currentTimeMillis()) {
                                Thread.sleep(1000);
                            }

                            System.out.println("Now you catch up");
                            System.out.println(time + " " + System.currentTimeMillis());
                            line = makeInsertLine(rs, rsMetaData, columnNames, numberOfColumns);
                            System.out.println(line);
                            st2.executeUpdate(line);

                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (st != null) {
                        st.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String makeInsertLine(ResultSet rs, ResultSetMetaData rsMetaData, Vector<String> columnNames, int numberOfColumns) {
        String insertLine = "INSERT INTO " + dbOut + " set ";
        try {
            if (!rsMetaData.isAutoIncrement(1)) {
                //dates need quotes
                if (rsMetaData.getColumnType(1) == java.sql.Types.TIMESTAMP) {
                    insertLine += columnNames.get(0) + "=\'" + rs.getString(columnNames.get(0)) + "\',";
                } else {
                    insertLine += columnNames.get(0) + "=" + rs.getString(columnNames.get(0)) + ",";
                }
            }
            for (int i = 0; i < numberOfColumns - 1; ++i) {
                if (!rsMetaData.isAutoIncrement(i + 1)) {
                    if (rsMetaData.getColumnType(i + 1) == java.sql.Types.TIMESTAMP) {
                        insertLine += columnNames.get(i) + "=\'" + rs.getString(columnNames.get(i)) + "\',";
                    } else {
                        insertLine += columnNames.get(i) + "=" + rs.getString(columnNames.get(i)) + ",";
                    }
                }
            }
            if (!rsMetaData.isAutoIncrement(numberOfColumns)) {
                //dates need quotes
                if (rsMetaData.getColumnType(numberOfColumns) == java.sql.Types.TIMESTAMP) {
                    insertLine += columnNames.get(numberOfColumns - 1) + "=\'" + rs.getString(columnNames.get(numberOfColumns - 1)) + "\"";
                } else {
                    insertLine += columnNames.get(numberOfColumns - 1) + "=" + rs.getString(columnNames.get(numberOfColumns - 1));
                }
            }
            insertLine += ";";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insertLine;
    }

}
