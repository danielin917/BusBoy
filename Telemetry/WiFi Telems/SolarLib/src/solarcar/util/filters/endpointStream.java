
package solarcar.util.filters;

import java.util.LinkedList;

public class endpointStream {
    LinkedList<Double> stream;
    int size;
    public endpointStream(int streamSize)
    {
        stream=new LinkedList<Double>();
        size=streamSize;
    }
    public double insert(double val)
    {
        stream.addFirst(val);
        if(stream.size()>size)
        {
            stream.removeLast();
        }
        return stream.peekFirst()-stream.peekLast();
    }
    public double get()
    {
        if(stream.size()==0)
        {
            return 0;
        }
        return stream.peekFirst()-stream.peekLast();
    }
    public void reset()
    {
        stream.clear();
    }
}
