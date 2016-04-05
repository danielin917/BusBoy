using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Solar;
using System.Threading;
using NetMQ;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.Concurrent;
using System.Runtime.Serialization;
using System.IO;
using System.Runtime.CompilerServices;
using Microsoft.CSharp;
using System.Runtime.Remoting;

/**
 * This class defines a testing framework for Databus. Notable are the following functions:
 * SendHeartbeat: sends a heartbeat to the DataBus
 * ReceiveRequest: waits for the DataBus to send a request, then returns the number it requested
 * SendRecordAndWait: sends a record on the Request socket, and returns the object that DataBus passed to its callback
 * SendValueAndWait: sends a record on the Heartbeat socket, and returns the object that DataBus passed to its callback
**/


namespace Solar
{
    [TestClass]
    public class DataBusBaseTest : IDisposable
    {
        public DataBus bus;
        public NetMQFrame clientUID;
        [TestInitialize]
        public void Init()
        {
            bus = new DataBus();
            bus.subscribeAll(Callback);
            bus.subscribeRecords(Callback);
            bus.subscribteFlush(Callback);
            bus.connect("localhost", 9980);
            System.Threading.Thread.Sleep(750); //annoying, but there's no way to block until ZMQ has established a connection

        }
        [TestCleanup]
        public void Cleanup()
        {
            bus.disconnect();
            return;
        }  


        /** The following code is used to allow synchronized reciept of messages.
         * That is, it allows us to send a message to the data bus, then receive it
         * in the same thread. */
        SemaphoreSlim waitSemaphore;
        Message waitMessage;
        RecordDescriptor waitRecord;
        Timer abortTimer;
        public NetMQContext context;
        public NetMQ.Sockets.PublisherSocket heartbeatSock;
        public NetMQ.Sockets.PublisherSocket valueSock;
        public NetMQ.Sockets.RouterSocket requesterSock;
        public DataBusBaseTest()
        {
            waitSemaphore = new SemaphoreSlim(0);
            context = NetMQContext.Create();

            //create connections
            heartbeatSock = context.CreatePublisherSocket();
            heartbeatSock.Bind("tcp://localhost:9980");

            requesterSock = context.CreateRouterSocket();
            requesterSock.Bind("tcp://localhost:9981");

            rand = new Random();
        }
        public Random rand;
        public void Dispose()
        {
            Dispose(true);
        }
        protected virtual void Dispose(bool dispose)
        {
            if (heartbeatSock != null)
            {
                heartbeatSock.Dispose();
            }
            if (requesterSock != null)
            {
                requesterSock.Dispose();
            }
            if (context != null)
            {
                context.Dispose();
            }
            heartbeatSock = null;
            requesterSock = null;
            context = null;
        }

        public void SendHeartbeat(uint message_id, string recordname, string json, uint checksum = 0) {
            byte[] record = MakeRecord(message_id, recordname, json, checksum);
            heartbeatSock.Send(record);
        }
        public uint ReceiveRequest()
        {
            var msg = requesterSock.ReceiveMessage();
            clientUID = msg[0];
            byte[] bytes = msg[1].Buffer;
            byte[] temp = new byte[4];
            for (int i = 0; i < 4; ++i)
            {
                temp[temp.Length - i - 1] = bytes[8 + i];
            }
            return BitConverter.ToUInt32(temp, 0);
        }
        public RecordDescriptor SendRecordAndWait(uint message_id, string recordname, string json, uint checksum = 0)
        {

            SendRecordNoWait(message_id, recordname, json, checksum);
            return RecordWait();
        }
        public void SendRecordNoWait(uint message_id, string recordname, string json, uint checksum = 0)
        {
            byte[] bytes = MakeRecord(message_id, recordname, json, checksum);
            NetMQMessage msg = new NetMQMessage();
            msg.Append(clientUID);
            //msg.AppendEmptyFrame();
            msg.Append(bytes);
            requesterSock.SendMessage(msg);
        }
        public RecordDescriptor RecordWait()
        {
            waitSemaphore.Wait();
            return waitRecord;
        }
        public Message SendValueAndWait(uint message_id, RecordDescriptor positioner, Dictionary<string, dynamic> values, uint checksum = 0)
        {
            SendValueNoWait(message_id, positioner, values, checksum);
            return ValueWait();
        }
        public void SendValueNoWait(uint message_id, RecordDescriptor positioner, Dictionary<string, dynamic> values, uint checksum = 0)
        {
            byte[] value = MakeValue(message_id, positioner, values, checksum);
            heartbeatSock.Send(value);
        }
        public Message ValueWait()
        {
            waitSemaphore.Wait();
            return waitMessage;
        }
        byte[] MakeValue(uint message_id, RecordDescriptor positioner, Dictionary<string, dynamic> values, uint checksum)
        {
            int messagelen = 12;
            foreach (KeyValuePair<string, ValueDescriptor> vd in positioner.harness)
            {
                if (vd.Value.size + 12 + vd.Value.offset > messagelen)
                {
                    messagelen = (int)vd.Value.size + 12 + (int)vd.Value.offset;
                }
            }
            byte[] bytes = new byte[messagelen];

            bytes[0] = (byte)'V';
            bytes[1] = (byte)'A';
            bytes[2] = (byte)'L';
            bytes[3] = (byte)'_';
            byte[] checksumBytes = BitConverter.GetBytes(checksum);
            for (int i = 0; i < checksumBytes.Length; ++i)
            {
                bytes[i + 4] = checksumBytes[i];
            }
            byte[] idBytes = BitConverter.GetBytes(message_id);
            //reverse endianness
            for (int i = 0; i < idBytes.Length; ++i)
            {
                bytes[11 - i] = idBytes[i];
            }

            foreach (KeyValuePair<string, ValueDescriptor> vd in positioner.harness)
            {
                int offset = 12 + (int)vd.Value.offset;
                Type dynamicType = (values[vd.Key]).GetType();
                byte[] valBytes;
                if(dynamicType == typeof(sbyte) || dynamicType == typeof(byte)) {
                    valBytes = new byte[1];
                    valBytes[0] = (byte)values[vd.Key];
                } else {
                    valBytes = BitConverter.GetBytes(values[vd.Key]);
                }
                copyover(bytes, valBytes, offset);
            }
            return bytes;
        }
        byte[] MakeRecord(uint message_id, string recordname, string json, uint checksum)
        {
            byte[] bytes = new byte[13 + recordname.Length + json.Length];
            bytes[0] = (byte)'R';
            bytes[1] = (byte)'E';
            bytes[2] = (byte)'C';
            bytes[3] = (byte)'_';
            byte[] checksumBytes = BitConverter.GetBytes(checksum);
            for (int i = 0; i < checksumBytes.Length; ++i)
            {
                bytes[i + 4] = checksumBytes[i];
            }
            byte[] idBytes = BitConverter.GetBytes(message_id);
            for(int i=0; i<idBytes.Length; ++i) {
                //flip endianness
                bytes[11-i] = idBytes[i];
            }
            byte[] nameBytes = Encoding.ASCII.GetBytes(recordname);
            for(int i=0; i<nameBytes.Length; ++i) {
                bytes[i+12] = nameBytes[i];
            }
            bytes[12 + recordname.Length] = (byte)'\0';
            byte[] jsonBytes = Encoding.ASCII.GetBytes(json);
            for(int i=0; i<json.Length; ++i) {
                bytes[i+13+nameBytes.Length] = jsonBytes[i];
            }
            return bytes;
            
        }
        void copyover(byte[] dest, byte[] src, int offset)
        {
            for (int i = 0; i < src.Length; ++i)
            {
                dest[i + offset] = src[i];
            }
        }
        void Callback(RecordDescriptor obj)
        {
            waitRecord = obj;
            waitSemaphore.Release(1);
        }
        void Callback(Message obj)
        {
            waitMessage = obj;
            waitSemaphore.Release(1);
        }
        void Callback()
        {
            waitSemaphore.Release(1);
        }
        public void WaitForFlush()
        {
            waitSemaphore.Wait();
        }
        public static string standardJson = @"
                {
                ""description"": ""Cruise control status"",
                ""harness"": {
                    ""byte_unsigned"": {
                        ""description"": ""byte_unsigned_value."",
                        ""offset"": 0,
                        ""size"": 1,
                        ""typename"": ""uint"",
                        ""unit"": ""furlongs""
                    },
                    ""byte_signed"": {
                        ""description"": ""byte_signed_value."",
                        ""offset"": 1,
                        ""size"": 1,
                        ""typename"": ""int"",
                        ""unit"": ""furlongs""
                    },
                    ""short_unsigned"": {
                        ""description"": ""short_unsigned_value."",
                        ""offset"": 2,
                        ""size"": 2,
                        ""typename"": ""uint"",
                        ""unit"": ""furlongs""
                    },
                    ""short_signed"": {
                        ""description"": ""short_signed_value."",
                        ""offset"": 4,
                        ""size"": 2,
                        ""typename"": ""int"",
                        ""unit"": ""furlongs""
                    },
                    ""int_unsigned"": {
                        ""description"": ""int_unsigned_value."",
                        ""offset"": 6,
                        ""size"": 4,
                        ""typename"": ""uint"",
                        ""unit"": ""furlongs""
                    },
                    ""int_signed"": {
                        ""description"": ""int_signed_value."",
                        ""offset"": 10,
                        ""size"": 4,
                        ""typename"": ""int"",
                        ""unit"": ""furlongs""
                    },
                    ""long_unsigned"": {
                        ""description"": ""long_unsigned_value."",
                        ""offset"": 14,
                        ""size"": 8,
                        ""typename"": ""uint"",
                        ""unit"": ""furlongs""
                    },
                    ""long_signed"": {
                        ""description"": ""long_signed_value."",
                        ""offset"": 22,
                        ""size"": 8,
                        ""typename"": ""int"",
                        ""unit"": ""furlongs""
                    },
                    ""float"": {
                        ""description"": ""float_value."",
                        ""offset"": 30,
                        ""size"": 4,
                        ""typename"": ""float"",
                        ""unit"": ""furlongs""
                    },
                    ""double"": {
                        ""description"": ""double_value."",
                        ""offset"": 34,
                        ""size"": 8,
                        ""typename"": ""float"",
                        ""unit"": ""furlongs""
                    }
                }
            }
            ";
    }
}

public static class Extr
{
    public static T[] Concat<T>(this T[] x, T[] y)
    {
        if (x == null) throw new ArgumentNullException("x");
        if (y == null) throw new ArgumentNullException("y");
        int oldLen = x.Length;
        Array.Resize<T>(ref x, x.Length + y.Length);
        Array.Copy(y, 0, x, oldLen, y.Length);
        return x;
    }
}