using System;
using Solar;

class Program
{
    static void Main(string[] args)
    {
        DataBus bus = new DataBus("localhost", 10000);
        bus.subscribe("bms_data", CallbackFunction);
        System.Threading.Thread.Sleep(System.Threading.Timeout.Infinite);
    }
    static void CallbackFunction(Message message)
    {
        Console.WriteLine(message.ToString());
    }
}
