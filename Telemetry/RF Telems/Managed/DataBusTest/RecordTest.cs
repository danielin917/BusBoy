using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Threading;
namespace Solar
{
    [TestClass]
    public class RecordTest : DataBusBaseTest
    {
        [TestMethod(), Timeout(3000)]
        public void BasicTest()
        {
            //this test only makes sure the constructor is working
            Assert.AreNotEqual(bus, null);
        }
        [TestMethod(), Timeout(3000)]
        public void HeartbeatTest()
        {
            //sends a heartbeat
            //receives a record request
            SendHeartbeat(3, "alpha", "");
            uint number = ReceiveRequest();
            Assert.AreEqual(number, 0u);
        }
        [TestMethod(), Timeout(3000)]
        public void DescriptorTest()
        {
            //sends a heartbeat
            //receives a record request
            //sends records
            //receives record objects
            //validates records
            SendHeartbeat(1, "alpha", "");
            uint number = ReceiveRequest();
            Assert.AreEqual(number, 0u);

            RecordDescriptor rec = SendRecordAndWait(0, "beta", "");
            Assert.AreEqual(rec.code, 0u);
            Assert.AreEqual(rec.name, "beta");

            RecordDescriptor rec2 = SendRecordAndWait(1, "gamma", @"
                {
                ""description"": ""Cruise control status"",
                ""harness"": {
                    ""limit"": {
                        ""description"": ""Current cruise limit."",
                        ""offset"": 8,
                        ""size"": 4,
                        ""typename"": ""float"",
                        ""unit"": ""m/s""
                    },
                    ""speed"": {
                        ""description"": ""Current cruise speed."",
                        ""offset"": 12,
                        ""size"": 4,
                        ""typename"": ""float"",
                        ""unit"": ""m/s""
                    },
                    ""timestamp"": {
                        ""description"": ""The message's timestamp."",
                        ""offset"": 0,
                        ""size"": 8,
                        ""typename"": ""int"",
                        ""unit"": ""uS""
                    }
                }
            }
            ");
            Assert.AreEqual(rec2.name, "gamma");
            Assert.AreEqual(1u, rec2.code);
            Assert.AreEqual(rec2.description, "Cruise control status");
            Assert.AreEqual(rec2.harness.Count, 3);

            Assert.AreEqual(rec2.harness["limit"].description, "Current cruise limit.");
            Assert.AreEqual(rec2.harness["limit"].offset, 8u);
            Assert.AreEqual(rec2.harness["limit"].size, 4u);
            Assert.AreEqual(rec2.harness["limit"].type, ValueDescriptor.Type.floatType);
            Assert.AreEqual(rec2.harness["limit"].unit, "m/s");

            Assert.AreEqual(rec2.harness["speed"].description, "Current cruise speed.");
            Assert.AreEqual(rec2.harness["speed"].offset, 12u);
            Assert.AreEqual(rec2.harness["speed"].size, 4u);
            Assert.AreEqual(rec2.harness["speed"].type, ValueDescriptor.Type.floatType);
            Assert.AreEqual(rec2.harness["speed"].unit, "m/s");

            Assert.AreEqual(rec2.harness["timestamp"].description, "The message's timestamp.");
            Assert.AreEqual(rec2.harness["timestamp"].offset, 0u);
            Assert.AreEqual(rec2.harness["timestamp"].size, 8u);
            Assert.AreEqual(rec2.harness["timestamp"].type, ValueDescriptor.Type.intType);
            Assert.AreEqual(rec2.harness["timestamp"].unit, "uS");
        }

        [TestMethod(), Timeout(3000)]
        public void HeartbeatTestRepeat() {
            //sends a heartbeat
            //receives a request
            //handles the request
            //sends the same heartbeat
            //sends a new heartbeat
            //receives a new requst
            SendHeartbeat(3, "three", "");
            uint number = ReceiveRequest();
            Assert.AreEqual(0u, number);
            SendRecordAndWait(0, "zero", "");
            SendRecordAndWait(1, "one", "");
            SendRecordAndWait(2, "two", "");
            SendRecordAndWait(3, "three", "");

            SendHeartbeat(3, "three", "");
            SendHeartbeat(4, "four", "");
            number = ReceiveRequest();
            Assert.AreEqual(3u, number);
            RecordDescriptor rec = SendRecordAndWait(4, "four", "");
            Assert.AreEqual("four", rec.name);
            Assert.AreEqual(4u, rec.code);
        }

        [TestMethod(), Timeout(3000)]
        public void ChecksumTest()
        {
            SendHeartbeat(0, "zero", "", 12);
            SendHeartbeat(1, "one", "", 14);
            uint number = ReceiveRequest();
            RecordDescriptor rec = SendRecordAndWait(0, "one", "", 14);
            Assert.AreEqual(14u, rec.checksum);
        }
        SemaphoreSlim sema1 = new SemaphoreSlim(0);
        SemaphoreSlim sema2 = new SemaphoreSlim(0);
        [TestMethod(), Timeout(4000)]
        public void ThreadSafetyTest()
        {
            //verifies that calls to disconnect() do not stop callbacks in progress
            SendHeartbeat(0, "v0", "");
            sema1 = new SemaphoreSlim(0);
            sema2 = new SemaphoreSlim(0);
            bus.subscribeRecords(ThreadSafetyCallback);
            SendHeartbeat(1, "foo", "");
            uint number = ReceiveRequest();
            SendRecordNoWait(1, "zero", "", 12);
            sema1.Wait();
            //at this point, we know ThreadSafetyCallback is running
            bus.disconnect();
            GC.Collect(1000, GCCollectionMode.Forced, true); //makes sure the garbage collector releases all resources, so we get an accurate test
            sema2.Release(); //let ThreadSafetyCallback continue
            sema1.Wait(); //wait for it to finish
        }
        public void ThreadSafetyCallback(RecordDescriptor r)
        {
            sema1.Release();
            //at this point, ThreadSafetyCallback must be running
            sema2.Wait();
            sema1.Release();
        }
        [TestMethod(), Timeout(3000)]
        public void NotTooManyTest()
        {
            bus.subscribeRecords(RecordCallbackCounter);
            //verifies that the DataBus makes exactly the correct number of record calls, not more
            SendHeartbeat(9, "foo", standardJson);
            uint start = ReceiveRequest();
            SendRecordAndWait(0, "v0", "");
            SendRecordAndWait(1, "a", standardJson);
            SendRecordAndWait(2, "b", standardJson);
            SendRecordAndWait(3, "c", standardJson);
            SendRecordAndWait(4, "d", standardJson);
            SendRecordAndWait(5, "e", standardJson);
            SendRecordAndWait(6, "f", standardJson);
            SendRecordAndWait(7, "g", standardJson);
            SendRecordAndWait(8, "h", standardJson);
            SendRecordAndWait(9, "foo", standardJson);
            Assert.AreEqual(10, recordCallbackCount);
            SendHeartbeat(9, "foo", standardJson);
            SendHeartbeat(9, "foo", standardJson);
            SendHeartbeat(10, "bar", standardJson);
            start = ReceiveRequest();
            Assert.AreEqual(9u, start);
            SendHeartbeat(10, "bar", standardJson);
            SendHeartbeat(10, "bar", standardJson);
            SendRecordAndWait(10, "bar", standardJson);
            Assert.AreEqual(11, recordCallbackCount);
        }
        int recordCallbackCount = 0;
        void RecordCallbackCounter(RecordDescriptor r)
        {
            recordCallbackCount++;
        }
        [TestMethod(), Timeout(3000)]
        public void FlushTest()
        {
            SendHeartbeat(1, "foo", standardJson);
            uint r = ReceiveRequest();
            RecordDescriptor v0record = SendRecordAndWait(0, "v0", "");
            RecordDescriptor fooRecord = SendRecordAndWait(1, "foo", standardJson);
            SendHeartbeat(0, "v1", ""); //flush
            WaitForFlush();
            RecordDescriptor v1record = SendRecordAndWait(0, "v1", "");
            SendHeartbeat(1, "bar", standardJson);
            RecordDescriptor barRecord = SendRecordAndWait(1, "bar", standardJson);
            Assert.AreNotEqual(v0record, v1record);
            Assert.AreNotEqual(fooRecord, barRecord);
            Assert.AreEqual("v1", v1record.name);
            Assert.AreEqual("bar", barRecord.name);

        }
    }
}
