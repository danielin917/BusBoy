using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using NetMQ;
using System.Collections.Concurrent;
using System.Threading;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.IO;
using System.Runtime.CompilerServices;
using System.Windows.Forms;

[assembly: InternalsVisibleTo("System.Runtime.Serialization.Json")]
namespace Solar
{
    public class Message
    {
        public RecordDescriptor meta {get; internal set; }
        public Message(byte[] bytes, RecordDescriptor meta)
        {
            this.meta = meta;
            this.values = new Dictionary<string, dynamic>();
            foreach(KeyValuePair<string, ValueDescriptor> vd in meta.harness) {
                int offset = 12 + (int)vd.Value.offset;
                if(vd.Value.type == ValueDescriptor.Type.floatType) {
                    if (vd.Value.size == 4)
                    {
                        values[vd.Key] = System.BitConverter.ToSingle(bytes, offset);
                    }
                    else if (vd.Value.size == 8)
                    {
                        values[vd.Key] = System.BitConverter.ToDouble(bytes, offset);
                    }
                    else
                    {
                        throw new Exception("Attempted to convert an object of size " + vd.Value.size + "to float/double. Must be size 4 or 8.");
                    }
                } else if(vd.Value.type == ValueDescriptor.Type.uintType) {
                    if (vd.Value.size == 1)
                    {
                        values[vd.Key] = bytes[offset];
                    }
                    else if (vd.Value.size == 2)
                    {
                        values[vd.Key] = System.BitConverter.ToUInt16(bytes, offset);
                    }
                    else if (vd.Value.size == 4)
                    {
                        values[vd.Key] = System.BitConverter.ToUInt32(bytes, offset);
                    }
                    else if (vd.Value.size == 8)
                    {
                        values[vd.Key] = System.BitConverter.ToUInt64(bytes, offset);
                    }
                    else
                    {
                        throw new Exception("Attempted to convert an object of size " + vd.Value.size + "To unsigned integer. Must be of size 1, 2, 4, 8");
                    }
                } else if(vd.Value.type == ValueDescriptor.Type.intType) {
                    if (vd.Value.size == 1)
                    {
                        values[vd.Key] = (sbyte)bytes[offset];
                    }
                    else if (vd.Value.size == 2)
                    {
                        values[vd.Key] = System.BitConverter.ToInt16(bytes, offset);
                    }
                    else if (vd.Value.size == 4)
                    {
                        values[vd.Key] = System.BitConverter.ToInt32(bytes, offset);
                    }
                    else if (vd.Value.size == 8)
                    {
                        values[vd.Key] = System.BitConverter.ToInt64(bytes, offset);
                    }
                    else
                    {
                        throw new Exception("Attempted to convert an object of size " + vd.Value.size + "To signed integer. Must be of size 1, 2, 4, 8");
                    }
                } else {
                    throw new Exception("Attempted to load unknown type.");
                }
            }
        }
        public Dictionary<string, dynamic> values {get; internal set; }
        public dynamic this[string valueName] {
            get {
                return values[valueName];
            }
            set {
                values[valueName] = value;
            }
        }
        private string _string;
        public override string ToString()
        {
            if (_string == null)
            {
                StringBuilder b = new StringBuilder();
                b.AppendLine(meta.name);
                foreach (KeyValuePair<string, dynamic> val in values)
                {
                    b.AppendLine("\t" + val.Key + ": " + val.Value);
                }
                _string = b.ToString();
            }
            return _string;
        }
    };
    [DataContract]
    public class RecordDescriptor
    {
        [DataMember]
        public Dictionary<string, ValueDescriptor> harness { get; internal set; }
        [DataMember]
        public string description { get; internal set; }
        public uint code { get; internal set; }
        public uint checksum { get; internal set; }
        public string name { get; internal set; }
        internal string _string;
        public override string ToString()
        {
            return _string;
        }

    }

    [DataContract]
    public class ValueDescriptor
    {
        private string _typename;
        [DataMember]
        public string typename { 
            get {
                return _typename; 
            }
            internal set {
                _typename = value;
                if(value == "uint") {
                    type = Type.uintType;
                } else if(value == "int") {
                    type = Type.intType;
                } else if(value == "float") {
                    type = Type.floatType;
                } else {
                    throw new Exception("No code defined for handling type " + value);
                }
            } 
        }
        private string _offsetstr;
        [DataMember(Name="offset")]
        public string offsetstr {
            get
            {
                return _offsetstr;
            }
            internal set
            {
                this.offset = Convert.ToUInt32(value);
                _offsetstr = value;
            } 
        }
        public uint offset {get; internal set;}
        [DataMember]
        public string description { get; internal set; }
        private string _sizestr;
        [DataMember(Name="size")]
        public string sizestr {
            get
            {
                return _sizestr;
            }
            internal set
            {
                this.size = Convert.ToUInt32(value);
                _sizestr = value;
            } 
        }
        public uint size {get; internal set; }
        [DataMember]
        public string unit { get; internal set; }
        public enum Type
        {
            uintType,
            intType,
            floatType
        }
        public Type type { get; internal set; }
    }

    public delegate void TMessageDelegate(Message message);
    public delegate void TRecordDelegate(RecordDescriptor message);
    public delegate void TFlushDelegate();
    public class DataBus
    {
        string hostname;
        uint port;

        public NetMQContext context;
        public NetMQ.Sockets.SubscriberSocket heartbeatSock;
        public NetMQ.Sockets.SubscriberSocket valueSock;
        public NetMQ.Sockets.DealerSocket requesterSock;

        ConcurrentDictionary<uint, RecordDescriptor> records;
        ConcurrentDictionary<string, ConcurrentBag<TMessageDelegate>> delegates;
        ConcurrentBag<TMessageDelegate> alwaysDelegates; //delegates which are called for EVERY value update
        ConcurrentBag<TRecordDelegate> recordDelegates; //delegates which are called when a new RECORD is received
        ConcurrentBag<TFlushDelegate> flushDelegates; //delegates which are called when records are flushed ( a new version record is received)

        Thread heartbeatThread;

        static uint GetIndex(string record)
        {
            var ret = GetIndex(Encoding.ASCII.GetBytes(record));
            return ret;
        }
        static uint GetIndex(byte[] bytes, int offset = 8)
        {
            //flip endianness
            byte[] temp = new byte[4];
            for (int i = 0; i < temp.Length; ++i)
            {
                temp[i] = bytes[offset - i + 3];
            }
            return BitConverter.ToUInt32(temp, 0);
        }
        static uint GetChecksum(byte[] bytes, int offset = 4)
        {
            return BitConverter.ToUInt32(bytes, offset);
        }
        static string GetRecordName(byte[] record)
        {
            StringBuilder builder = new StringBuilder();
            int index = 12;

            while (record.Length > index && record[index] != '\0')
            {
                char toAppend = (char)record[index];
                builder.Append(toAppend);
                index++;
            }
            return builder.ToString();
        }
        static string GetJson(byte[] record)
        {
            StringBuilder builder = new StringBuilder();
            int index = GetRecordName(record).Length + 13;
            while (record.Length > index && record[index] != '\0')
            {
                builder.Append((char)record[index]);
                index++;
            }
            return builder.ToString();
            
        }
        RecordDescriptor GetDescriptions(string json)
        {
            MemoryStream stream = new MemoryStream(Encoding.UTF8.GetBytes(json));
            DataContractJsonSerializerSettings settings = new DataContractJsonSerializerSettings();
            settings.UseSimpleDictionaryFormat = true;
            DataContractJsonSerializer js = new DataContractJsonSerializer(typeof(RecordDescriptor), settings);
            RecordDescriptor record = (RecordDescriptor)js.ReadObject(stream);
            record._string = json;
            return record;
        }
        private void Init()
        {
            //inialize concurrent structures
            records = new ConcurrentDictionary<uint, RecordDescriptor>();
            delegates = new ConcurrentDictionary<string, ConcurrentBag<TMessageDelegate>>();
            alwaysDelegates = new ConcurrentBag<TMessageDelegate>();
            recordDelegates = new ConcurrentBag<TRecordDelegate>();
            flushDelegates = new ConcurrentBag<TFlushDelegate>();
        }
        public DataBus()
        {
            Init();
        }
        public DataBus(string hostname, uint port) {
            Init();
            this.hostname = hostname;
            this.port = port;
            connect();
        }
        public void connect(string hostname, uint port) {
            this.hostname = hostname;
            this.port = port;
            connect();
        }
        void connect()
        {
            //clean up any existing connection
            if (context != null)
            {
                disconnect();
            }
            //create context
            context = NetMQContext.Create();

            //create connections
            heartbeatSock = context.CreateSubscriberSocket();
            heartbeatSock.Options.ReceiveHighWatermark = 1000;
            heartbeatSock.Connect("tcp://" + hostname + ":" + Convert.ToString(port));
            heartbeatSock.Subscribe("");

            requesterSock = context.CreateDealerSocket();
            requesterSock.Options.Linger = new System.TimeSpan(0);
            requesterSock.Options.SendHighWatermark = 1000;
            requesterSock.Connect("tcp://" + hostname + ":" + Convert.ToString(port + 1));

            //start the threads
            heartbeatThread = new Thread(new ThreadStart(HeartbeatThread));
            heartbeatThread.IsBackground = true;
            heartbeatThread.Start();
        }



        void HeartbeatThread()
        {
            try
            {
                int maxRecordCode = -1;
                int nextVersionNumber = 1;
                while (true)
                {
                    byte[] recordBytes = heartbeatSock.Receive();
                    if (recordBytes[0] == 'R')
                    {
                        uint recordCode = GetIndex(recordBytes);
                        string recordName = GetRecordName(recordBytes);
                        if (recordCode == 0 && recordName[0] == 'v')
                        {
                            string strVersionNumber = recordName.Remove(0, 1);
                            int versionNumber = Convert.ToInt32(strVersionNumber);
                            if (versionNumber >= nextVersionNumber)
                            {
                                //we have an actual new version record, so flush
                                Flush();
                                maxRecordCode = -1;
                                nextVersionNumber = versionNumber + 1;
                            }
                        }
                        if (recordCode > maxRecordCode)
                        {
                            uint checksum = (maxRecordCode <= 0) ? 0 : (records[recordCode - 1].checksum);
                            if (maxRecordCode == -1) maxRecordCode = 0;
                            RequestRecords((uint)maxRecordCode, recordCode, checksum);
                            maxRecordCode = (int)recordCode;
                        }
                    }
                    else if (recordBytes[0] == 'V')
                    {
                        uint recordCode = GetIndex(recordBytes);
                        RecordDescriptor record = records[recordCode];
                        //construct message
                        Message message = new Message(recordBytes, record); //temporary
                        if (delegates.ContainsKey(record.name))
                        {
                            foreach (TMessageDelegate mdelegate in delegates[record.name])
                            {
                                Delegate(mdelegate, message);
                            }
                        }
                        foreach (TMessageDelegate mdelegate in alwaysDelegates)
                        {
                            Delegate(mdelegate, message);
                        }
                    }
                }
            }
            catch (NetMQ.TerminatingException ex)
            {
                /*. exit thread .*/
            }
        }

        bool doneVersionCallback = false;
        //note: fromRecord is exclusive, toRecord is inclusive
        void RequestRecords(uint fromRecord, uint toRecord, uint fromChecksum)
        {
            //make the request message
            byte[] startCodeBytes = BitConverter.GetBytes(fromRecord);
            byte[] endCodeBytes = BitConverter.GetBytes(toRecord);
            byte[] checksumBytes = BitConverter.GetBytes(fromChecksum);
            
            byte[] request = new byte[4 + checksumBytes.Length + startCodeBytes.Length + endCodeBytes.Length];
            request[0] = (byte)'R';
            request[1] = (byte)'E';
            request[2] = (byte)'Q';
            request[3] = (byte)'_';
            for (int i = 0; i < checksumBytes.Length; ++i)
            {
                request[i + 4] = checksumBytes[i];
            }
            //reverse endiannesss
            for (int i = 0; i < startCodeBytes.Length; ++i)
            {
                request[checksumBytes.Length + 3 + startCodeBytes.Length - i] = startCodeBytes[i];
            }
            for (int i = 0; i < startCodeBytes.Length; ++i)
            {
                request[checksumBytes.Length + 3 + startCodeBytes.Length + endCodeBytes.Length - i] = endCodeBytes[i];
            }


            //send the message
            requesterSock.Send(request);
            try
            {
                while (true)
                {
                    byte[] recordStr = requesterSock.Receive();
                    uint recordCode = GetIndex(recordStr);
                    AppendRecord(recordStr);
                    if (recordCode == toRecord) break;
                }
            }
            catch (NetMQ.TerminatingException)
            {
                /*. exit thread .*/
            }
            
        }
        RecordDescriptor AppendRecord(byte[] recordStr)
        {
            uint recordCode = GetIndex(recordStr);
            uint recordChecksum = GetChecksum(recordStr);
            string recordName = GetRecordName(recordStr);
            string json = GetJson(recordStr);
            RecordDescriptor record;
            if (json != "")
            {
                record = GetDescriptions(json);
            }
            else
            {
                record = new RecordDescriptor();
            }
            record.code = recordCode;
            record.checksum = recordChecksum;
            record.name = recordName;

            records[recordCode] = record;

            //if the client has decided to listen for records, delegate to them
            if (recordName[0] == 'v' && json == "" && recordCode == 0u)
            {
                if (doneVersionCallback == false)
                {
                    doneVersionCallback = true;
                    foreach (TRecordDelegate del in recordDelegates)
                    {
                        Delegate(del, record);
                    }
                }
            }
            else
            {
                foreach (TRecordDelegate del in recordDelegates)
                {
                    Delegate(del, record);
                }
            }
            return record;
        }
        void Delegate<MessageType>(dynamic mdelegate, MessageType message)
        {
            if (mdelegate.Target is Form)
            {
                object[] objs = new object[1];
                objs[0] = message;
                ((Form)mdelegate.Target).Invoke(mdelegate, objs);
            }
            else
            {
                mdelegate.Invoke(message);
            }
        }
        void Delegate(dynamic mdelegate)
        {
            if (mdelegate.Target is Form)
            {
                ((Form)mdelegate.Target).Invoke(mdelegate);
            }
            else
            {
                mdelegate.Invoke();
            }
        }
        void Flush()
        {
            records.Clear();
            doneVersionCallback = false;
            foreach (TFlushDelegate del in flushDelegates)
            {
                Delegate(del);
            }
        }
        public void subscribe(string recordName, TMessageDelegate mdelegate)
        {
            ConcurrentBag<TMessageDelegate> dels = new ConcurrentBag<TMessageDelegate>();
            dels = delegates.GetOrAdd(recordName, dels);
            dels.Add(mdelegate);
        }
        public void subscribeAll(TMessageDelegate mdelegate)
        {
            alwaysDelegates.Add(mdelegate);
        }
        public void subscribeRecords(TRecordDelegate rdelegate)
        {
            recordDelegates.Add(rdelegate);
        }
        public void subscribteFlush(TFlushDelegate fdelegate)
        {
            flushDelegates.Add(fdelegate);
        }
        public void disconnect()
        {
            if (heartbeatSock != null) heartbeatSock.Close();
            if (valueSock != null) valueSock.Close();
            if (requesterSock != null) requesterSock.Close();
            if (context != null) context.Terminate();
            if (context != null) context.Dispose();
            context = null;
        }

    }
}