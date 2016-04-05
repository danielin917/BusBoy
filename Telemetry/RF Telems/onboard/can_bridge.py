import crcmod
import json
import multiprocessing
import Queue
import serial
import socket
import struct
import sys
import threading
import time
import zmq


import data_bus
import wire


UINT32_T = struct.Struct("I")
COUNTER = struct.Struct("<IHHHHBBB")

STANDARD_RTR = 1
EXTENDED_RTR = 2
EXTENDED_ID = 4

class CanBridge(object):

    def __init__(self, device, baudrate, databus):
        self.device = device
        self.ser = serial.Serial(device, baudrate, timeout=0.02)

        # rx, tx, rx_overrun, tx_overrun, can_error, can_merror, radio_error
        # bandwidth, dongle_slip
        self.counters = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
        self.last_raw_counters = None
        self.crc = crcmod.mkCrcFun(0x11021, 0x0, True, 0x0)
        self.rx_queue = multiprocessing.Queue(maxsize=10000)

        self.databus = databus
        self.databus.flush_callback.append(self.HandleFlush)
        self.HandleFlush()

        self.thread = threading.Thread(target=self.Run)
        self.thread.daemon = True
        self.thread.start()

        self.discard_count = 100

    def HandleFlush(self):
        self._next = None
        print "Flushing"
        values = {}
        values["timestamp"] = {"typename":"int", "size":8, "offset":0,
                               "unit":"uS",
                               "description": "The bridge's timestamp."}
        values["canid"] = {"typename":"int", "size":4, "offset":8,
                           "printf":"0x%X",
                           "description":"The ID of the CAN message. Flag bits 30 - RTR, 31 - ERTR, 32 - Extended"}
        values["dlc"] = {"typename":"int", "size":1, "offset":13,
                         "unit":"bytes",
                         "description":"Length of the CAN message data."}
        values["data"] = {"typename":"bytes", "size":8, "offset":14,
                          "description":"The data."}

        metadata = {"harness":values,
                    "mux":"canid",
                    "description":"Raw CAN message data received from a bridge."}

        hostname = socket.gethostname()
        self._rx_record = self.databus.AddRecord("can.rx.%s.%s\0%s" % (
            hostname,
            self.device,
            json.dumps(metadata)))
        self._harness = wire.Harness(values)
        fields = ("timestamp", "canid", "dlc", "data")
        self._wires = [self._harness[field] for field in fields]

        counters = {}
        counters["now"] = {"typename":"int", "size":8, "offset":0,
                           "unit":"uS",
                           "description": "The bridge's current time."}
        counters["rx"] = {"typename":"int", "size":8, "offset":8,
                          "unit":"messages",
                          "description":"Number of messages received from CAN."}
        counters["tx"] = {"typename":"int", "size":8, "offset":16,
                          "unit":"messages",
                          "description":"Number of messages sent over CAN."}
        counters["rx_overrun"] = {"typename":"int", "size":8, "offset":24,
                                  "unit":"messages",
                                  "description":"Number of dropped messages due to radio FIFO."}
        counters["tx_overrun"] = {"typename":"int", "size":8, "offset":32,
                                  "unit":"messages",
                                  "description":"Number of dropped messages due to CAN FIFO."}
        counters["can_error"] = {"typename":"int", "size":8, "offset":40,
                                 "unit":"errors",
                                 "description":"Error in the CAN controller."}
        counters["can_msg_error"] = {"typename":"int", "size":8, "offset":48,
                                     "unit":"errors",
                                     "description":"Message error on the bus."}
        counters["radio_flush"] = {"typename":"int", "size":8, "offset":56,
                                   "unit":"flushes",
                                   "description":"Number of times the radio flushed due to being blocked."}
        counters["bandwidth"] = {"typename":"int", "size":4, "offset":64,
                                 "unit":"messages/s",
                                 "description":"Link bandwidth in CAN messages."}
        counters["dongle_slip"] = {"typename":"int", "size":4, "offset":72,
                                   "unit":"bytes",
                                   "description":"Bytes slipped due to syncing with the dongle."}

        counters_metadata = {"harness":counters,
                             "description":"Bridge performance statistics."}


        self._counters_record = self.databus.AddRecord(
            "can.counters.%s.%s\0%s" % (
                hostname,
                self.device,
                json.dumps(counters_metadata)))
        self._counters_harness = wire.Harness(counters)
        fields = ("now", "rx", "tx", "rx_overrun", "tx_overrun", "can_error",
                  "can_msg_error", "radio_flush", "bandwidth", "dongle_slip")
        self._counters_wires = [self._counters_harness[field] for field in fields]


    def Poll(self, now, next, pollin, *args):
        if not self.databus.bootstrapped:
            return

        if self._next is None:
            self._next = now

        # Transmit any telemetry
        try:
            while True:
                for wire, value in zip(self._wires, self.rx_queue.get_nowait()):
                    wire.value = value
                self._rx_record.SetValue(self._harness.buf)
        except Queue.Empty:
            pass

        pollin.append(self.rx_queue._reader)

        # Publish periodic counters.
        while now >= self._next:
            self._next += 1.0
            for wire, value in zip(self._counters_wires, self.counters):
                wire.value = value
            self.counters[8] = 0
            self._counters_record.SetValue(self._counters_harness.buf)

        next.append(self._next)

    def Run(self):
        buf = ""
        cursor = 0
        while True:
            while len(buf) - cursor >= 3:
                # Assume synced.
                length = ord(buf[cursor + 2])
                if buf[cursor] != '\xFF' or buf[cursor + 1] != '\xFF' or length > 32:
                    # Pop a byte and try again.
                    self.counters[9] += 1
                    buf = buf[cursor + 1:]
                    cursor = 0
                    continue
                if len(buf) - cursor >= length + 3:
                    self.HandlePayload(buffer(buf, cursor + 3, length))
                    cursor += 3 + length
                else:
                    # Need to wait for the rest of the payload.
                    buf = buf[cursor:]
                    cursor = 0
                    break

            buf += self.ser.read(2048)

    def HandlePayload(self, payload):
        if len(payload) != 31:
            return
        expected_crc = ord(payload[-2]) | (ord(payload[-1])<<8)
        computed_crc = self.crc(buffer(payload, 0, len(payload)-2))
        if expected_crc != computed_crc:
            return

        raw_counters = COUNTER.unpack_from(buffer(payload))

        if self.last_raw_counters is None:
            self.last_raw_counters = raw_counters

        if self.discard_count > 0:
            self.discard_count -= 1
        else:
            self.counters[0] += (raw_counters[0] -
                                 self.last_raw_counters[0]) & 0xFFFFFFFF
            self.counters[1] += (raw_counters[1] -
                                 self.last_raw_counters[1]) & 0xFFFF
            self.counters[2] += (raw_counters[2] -
                                 self.last_raw_counters[2]) & 0xFFFF
            self.counters[3] += (raw_counters[3] -
                                 self.last_raw_counters[3]) & 0xFFFF
            self.counters[4] += (raw_counters[4] -
                                 self.last_raw_counters[4]) & 0xFFFF
            self.counters[5] += (raw_counters[5] -
                                 self.last_raw_counters[5]) & 0xFF
            self.counters[6] += (raw_counters[6] -
                                 self.last_raw_counters[6]) & 0xFF
            self.counters[7] += (raw_counters[7] -
                                 self.last_raw_counters[7]) & 0xFF
            self.counters[8] += 1 # CAN message bandwidth

        self.last_raw_counters = raw_counters

        if payload[15] != '\x00':
            timestamp = self.counters[0]
            canid = ((ord(payload[20]) & 0x7) << 29 |
                     UINT32_T.unpack_from(payload, offset=16)[0])
            length = (ord(payload[20]) >> 4)
            data = payload[21:29]
            
            try:
                self.rx_queue.put((timestamp, canid, length, data))
            except Queue.Full:
                print "RX Queue full. Dropping CAN frame."


if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='CAN telemetry bridge.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-d', '--device', type=str, default="/dev/ttyACM0",
                        help='Device to connect to')
    parser.add_argument('-b', '--baudrate', type=int, default=2000000,
                        help='Device baudrate')

    args = parser.parse_args()

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    bridge = CanBridge(args.device, args.baudrate, dc)

    while True:
        now = time.time()
        next = [now + 10.0]
        pollin = []
        pollout = []
        pollerr = []
        dc.Poll(now, next, pollin, pollout, pollerr)
        bridge.Poll(now, next, pollin, pollout, pollerr)
        timeout = min(next) - time.time()
        if timeout > 0:
            zmq.select(pollin, pollout, pollerr, timeout = timeout)

