import json
import struct
import time
from datetime import date
import zmq
import threading
import string

import data_bus
import wire
import platform



class LegacyLogger(object):
    def __init__(self, databus, log):
        print 'Logger'
        self.dc = databus
        self.dc.flush_callback.append(self.HandleFlush)
        self.dc.record_callback.append(self.HandleNewRecord)
        self.date = time.strftime('%c')
        operatingSystem = platform.system()
        if operatingSystem == 'Windows':
            dateString = self.date
            dateString = dateString.replace('/', '-')
            dateString = dateString.replace(':', '_')
            myDirname = os.path.normpath(os.environ['Solartelemetry'] + "/2015/onboard/logs/")
            myDirname = myDirname + '\\' + dateString
            self.LogFile = open(myDirname, "w")
        else:
            self.LogFile = open('logs/' + self.date, 'w')

        self.HandleFlush()
        self.pollThread = threading.Thread(target=self.Poll)
        self.pollThread.daemon = True 
        self.pollThread.start()

    def Poll(self):
        while True:
            self.dc.Poll(time.time(), [], [], [], [])
            print 'poll'
            time.sleep(10)

    def HandleFlush(self):
        pass

    def HandleNewRecord(self, record):
        fields = record.field.split("\0")
        if len(fields) <= 1:
            return
        metadata = json.loads(fields[1])
        if not "harness" in metadata:
            return
        if fields[0].startswith("can."):
            return

        record.name = fields[0].replace(".", "_").replace("/", "_").replace("-", "_")
        record.harness = wire.Harness(metadata["harness"])
        record.Subscribe()
        record.value_callback.append(self.HandleRx)

    def HandleRx(self, record):
        record.harness.buf = buffer(record.value)
        #packet = " ".join([record.name] + ['%s="%s"' % (key, wire) for key, wire in record.harness.items()])
        #print >>self.LogFile, "%f," % time.time(), packet
        packet = " ".join(['%s="%s"' % (key, wire) for key, wire in record.harness.items()])
        print >>self.LogFile, 'time=', time.strftime('%c'), "%f," % time.time(), 'id=', record.name + ',', packet

if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='Parses raw CAN messages.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-l', '--log', type=str, default="logs/aurum.txt",
                        help='Log file to append to.')

    args = parser.parse_args()

    if not args.connect:
        raise Exception("Must be connected to at least one data bus.")

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    bridge = LegacyLogger(dc, open(args.log, "a"))

    while True:
        now = time.time()
        next = [now + 10.0]
        pollin = []
        pollout = []
        pollerr = []
        dc.Poll(now, next, pollin, pollout, pollerr)
        timeout = min(next) - time.time()
        if timeout > 0:
            zmq.select(pollin, pollout, pollerr, timeout = timeout)
