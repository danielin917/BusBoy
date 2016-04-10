package solarcar.gui;
import java.util.LinkedList;

public class averageStream {
    LinkedList<Double> stream;
    int size;
    double avg;
    public averageStream(int streamSize)
    {
        stream=new LinkedList<Double>();
        size=streamSize;
    }
    public double insert(double val)
    {
        if(stream.size()<size)
        {
            stream.addFirst(val);
            avg=0;
            for(Double d: stream)
            {
                avg+=d;
            }
            avg/=stream.size();
        } else {
           double temp=stream.removeLast();
           avg-=temp/size;
           avg+=val/size;
           stream.addFirst(val);
        }
        return avg;
    }
    public double get()
    {
        if(stream.size()==0)
        {
            return 1;
        }
        return avg;
    }
}
