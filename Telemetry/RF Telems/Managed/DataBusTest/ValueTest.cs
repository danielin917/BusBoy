using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Solar;
using System.Collections.Generic;
using System.Threading;
namespace Solar
{
    [TestClass]
    public class ValueTest : DataBusBaseTest
    {
        [TestMethod(), Timeout(3000)]
        public void FieldTest()
        {
            SendHeartbeat(0, "v0", "");
            ReceiveRequest();
            SendRecordAndWait(0, "v0", "");
            RecordDescriptor positioner = EstablishRecord(1, "cruise");
            //we do this ten times to make the catching of edge cases more likely
            for (int i = 0; i < 10; ++i)
            {
                Dictionary<string, dynamic> values = GenerateValues();

                Message result = SendValueAndWait(1, positioner, values);

                foreach (KeyValuePair<string, dynamic> vd in values)
                {
                    dynamic expected = values[vd.Key];
                    dynamic actual = values[vd.Key];
                    Assert.AreEqual(values[vd.Key], result[vd.Key]);
                }
            }
        }
        [TestMethod(), Timeout(3000)]
        public void MetaTest() {
            SendHeartbeat(0, "v0", "");
            ReceiveRequest();
            SendRecordAndWait(0, "v0", "");
            RecordDescriptor meta = EstablishRecord(1, "puddles");
            Dictionary<string, dynamic> values = GenerateValues();
            Message result = SendValueAndWait(1, meta, values);
            Assert.AreEqual(meta, result.meta);
        }
        [TestMethod(), Timeout(4000)]
        public void ReconnectTest()
        {
            //verifies that disconnecting and reconnecting works
            SendHeartbeat(0, "v0", "");
            RecordDescriptor meta = EstablishRecord(1, "puddles");
            bus.disconnect();
            bus.connect("localhost", 9980);
            Dictionary<string, dynamic> values = GenerateValues();
            Message result = SendValueAndWait(1, meta, values);
            Assert.AreEqual(meta, result.meta);
            foreach (KeyValuePair<string, dynamic> vd in values)
            {
                dynamic expected = values[vd.Key];
                dynamic actual = values[vd.Key];
                Assert.AreEqual(values[vd.Key], result[vd.Key]);
            }
        }
        SemaphoreSlim sema1;
        SemaphoreSlim sema2;
        [TestMethod(), Timeout(3000)]
        public void ValueThreadSafetyTest()
        {
            SendHeartbeat(0, "v0", "");
            ReceiveRequest();
            SendRecordAndWait(0, "v0", "");
            //verifies that calls to disconnect() do not stop callbacks in progress
            sema1 = new SemaphoreSlim(0);
            sema2 = new SemaphoreSlim(0);
            bus.subscribe("foo", ThreadSafetyCallback);
            RecordDescriptor positioner = EstablishRecord(1, "foo");
            Dictionary<string, dynamic> values = GenerateValues();
            SendValueNoWait(1, positioner, values, 511);
            sema1.Wait();
            //at this point, we know ThreadSafetyCallback is running
            bus.disconnect();
            GC.Collect(1000, GCCollectionMode.Forced, true); //makes sure the garbage collector releases all resources, so we get an accurate test
            sema2.Release(); //let ThreadSafetyCallback continue
            sema1.Wait(); //wait for it to finish
        }
        public void ThreadSafetyCallback(Message r)
        {
            sema1.Release();
            //at this point, ThreadSafetyCallback must be running
            sema2.Wait();
            sema1.Release();
        }

        RecordDescriptor EstablishRecord(uint code, string name)
        {
            this.SendHeartbeat(code, name, standardJson);
            uint c = this.ReceiveRequest();
            return SendRecordAndWait(code, name, standardJson);
        }
        Dictionary<string, dynamic>  GenerateValues()
        {
            Dictionary<string, dynamic> values = new Dictionary<string, dynamic>();
            values["byte_unsigned"] = (byte)rand.Next(2 << 8);
            values["byte_signed"] = (sbyte)rand.Next(2 << 16);
            values["short_unsigned"] = (ushort)rand.Next(2 << 16);
            values["short_signed"] = (short)rand.Next(2 << 16);
            values["int_unsigned"] = (uint)rand.Next();
            values["int_signed"] = (int)rand.Next();
            values["long_unsigned"] = (ulong)(((ulong)rand.Next() << 32) + ((ulong)rand.Next()));
            values["long_signed"] = (long)(((long)rand.Next() << 32) + ((long)rand.Next()));
            values["float"] = (float)rand.NextDouble();
            values["double"] = rand.NextDouble();
            return values;
        }
        [TestMethod()]
        public void ValueFlushTest()
        {
            SendHeartbeat(1, "foo", standardJson);
            uint r = ReceiveRequest();
            RecordDescriptor v0record = SendRecordAndWait(0, "v0", "");
            RecordDescriptor fooRecord = SendRecordAndWait(1, "foo", standardJson);
            SendHeartbeat(0, "v1", ""); //flush
            WaitForFlush();
            RecordDescriptor v1record = SendRecordAndWait(0, "v1", "");
            SendHeartbeat(1, "bar", standardJson);
            RecordDescriptor barRecord = SendRecordAndWait(1, "bar", alternateJson);
            Dictionary<string, dynamic> values = new Dictionary<string,dynamic>();
            values["pie"] = (sbyte) 7;
            Message message = SendValueAndWait(1, barRecord, values);
            Assert.AreEqual(barRecord, message.meta);
            Assert.AreEqual(1, message.values.Count);

        }
        public static string alternateJson = @"
                {
                ""description"": ""Alternate Json"",
                ""harness"": {
                    ""pie"": {
                        ""description"": ""foo."",
                        ""offset"": 0,
                        ""size"": 1,
                        ""typename"": ""int"",
                        ""unit"": ""weasels""
                    }
                }
            }
            ";
    }

}
