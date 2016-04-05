/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telemetrylogger2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import static telemetrylogger2.TelemetryLogger.canIDs;

/**
 *
 * @author Leda Daehler
 */
public class dataOut extends TelemetryLogger implements Runnable {

    private Thread outThread;
    Statement st = null;
    double timeTaken = 0;
    int startTime, endTime;

    public void init() {
        //create databases for appropriate canIDs
        try {
            st = conn.createStatement();
            for (int i = 0; i < canIDs.size(); ++i) {
                String line = makeTable(canCols.get(i));
                st.executeUpdate(line);
            }
        } catch (SQLException e) {
        }
        outThread = new Thread(this);
        try {
            Thread.sleep(1000);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        outThread.start();        
    }

    @Override
    public void run() {
        System.out.println("here");
        while (true) {
            if (TelemetryLogger.conn != null) {
                startTime = (int)System.currentTimeMillis();
                segID = getCurrentSegment();
                query();
                endTime = (int)System.currentTimeMillis();
                
            }
            try {
                Thread.sleep(1000/rate - (endTime - startTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String makeTable(Vector<String> columnNames) {
        String insertLine = "CREATE TABLE IF NOT EXISTS `telemetry`.`" + columnNames.get(0) + "`(";
        try {
            insertLine += "`vdctime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,";

            for (int m = 1; m < columnNames.size(); ++m) {
                insertLine += "`" + columnNames.get(m) + "` double default null,";
            }
            insertLine += "  `badData` tinyint(1) DEFAULT '0',\n" + "  `segId` int(11) NOT NULL);";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return insertLine;
    }
    static int m = 0;
    static int sizeToWrite = 0;
    public void query() {
        ResultSet rs;
        try {
            Statement st = TelemetryLogger.conn.createStatement();
            //average data to be written
            while (rawCanData.isEmpty()){
                 Thread.sleep(1000 / rate);               
            }
            Vector<Vector<Double>> canData = new Vector<> (rawCanData);
            rawCanData.clear();
           /// rawCanData = null;
            for (int j = 0; j < canData.size(); ++j){
                rawCanData.add(new Vector<Double>(0));
            }
           // rawCanData = new Vector<>(blankData);
            System.out.println("CanSize: " + canData.get(0).size());
            System.out.println("RawSize: " + rawCanData.get(0).size());

            Vector<Vector<Double>> dataToWrite = new Vector<>();
            
            for (int k = 0; k < canData.size(); ++k) {        //for each canID
                int datSize = canCols.get(k).size() - 1;
                sizeToWrite = canData.get(k).size();
                if (sizeToWrite <=0 ) continue;
                Vector<Double> datOut = new Vector<>(datSize);
                datOut.setSize(datSize);
                for (int n = 0; n < datSize; ++n)
                    datOut.set(n,0.0);
                
                System.out.println(canData.get(k));
                System.out.println(datSize + " " + sizeToWrite);
                for (m = 0; m < sizeToWrite; ++m) {
                    datOut.set((m % datSize), datOut.get(m % datSize) + canData.get(k).get(m));
                }
                for (int j=0; j < datSize; ++j){
                    datOut.set(j,datOut.get(j)/sizeToWrite*datSize);
                }
                dataToWrite.add(datOut);
                String query = makeInsertLine(canCols.get(k), datOut);
                st.executeUpdate(query);
            }   
            
            canData = null;
            
        } catch (Exception ex) {
            System.out.println(m + " " + sizeToWrite);
            System.out.println(ex.getMessage());
        }
    }

    private static String makeInsertLine(Vector<String> columns, Vector<Double> data) {
        String insertLine = "INSERT INTO telemetry." + columns.get(0) + " set ";
        try {
            for (int i = 0; i < columns.size()-1; ++i) {
                insertLine += columns.get(i + 1) + "=" + data.get(i) + ",";
            }
            insertLine += "badData=0, segId=" + segID + ";";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return insertLine;
    }

    private void Vector(int datSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
