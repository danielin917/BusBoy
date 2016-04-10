/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import solarcar.vdcListener.VDCConn;
import solarcar.vdcListener.DataMessageSubscriber;
import solarcar.vdcListener.SolarDataMessage;
import solarcar.vdcListener.MessageHandler;

/**
 *
 * @author Ian
 */
public class parseMessage implements DataMessageSubscriber {
    String[] keys;
    public void init(String command)
    {
        keys = command.split("\\s+");
        MessageHandler.get().subscribeData(keys[0], this);
    }
    public void parseDataMessage(SolarDataMessage message)
    {
        String outstr=new String();
        for(int i=1;i<keys.length;++i)
        {
            Double got=message.get(keys[i]);
            if(got!=null)
            {
                outstr+=keys[i]+" "+got+" ";
            }
        }
        if(outstr!=null)
        {
            System.out.println("$ "+Long.toString(System.currentTimeMillis())+" "+segID.acquireCurrentSegment()+" "+keys[0]+" "+message.getType()+" "+outstr);
        }
        
    }
}
