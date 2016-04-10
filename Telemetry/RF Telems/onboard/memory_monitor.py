import json
import socket
import subprocess
import time
import zmq

import data_bus
import wire

class MemoryMonitor(object):
    def __init__(self, databus):
        self.dc = databus
        self.dc.flush_callback.append(self.HandleFlush)
        self.HandleFlush()

    def HandleFlush(self):
        self._next = None
        print "Flushing mem"
        lines = subprocess.check_output("free", shell=True).split("\n")
        values = {}
        values["timestamp"] = {"typename":"float", "size":8, "offset":0,
                               "unit":"seconds",
                               "description": "Time since the Unix epoch."}
        offset = 8
        fields = lines[0].split()
        for field in fields:
            values[field] = {"typename":"int", "size":8, "offset":offset,
                             "unit":"bytes"}
            offset += 8

        hostname = socket.gethostname()
        metadata = {"harness":values,
                    "description":"Memory usage statistics for %s." % hostname,
                    "hostname":hostname}

        self._record = self.dc.AddRecord("memory.%s\0%s" % (hostname,
                                                            json.dumps(metadata)))
        self._harness = wire.Harness(values)
        self._wires = [self._harness[field] for field in fields]

    def Poll(self, now, next, *args):
        if not self.dc.bootstrapped:
            return

        if self._next is None:
            self._next = now

        while now >= self._next:
            self._next += 1.0
            lines = subprocess.check_output(["free", "-b"], shell=False).split("\n")
            line = lines[1].split()[1:]
            self._harness.timestamp.value = self._next
            for wire, value in zip(self._wires, line):
                wire.value = int(value)
            self._record.SetValue(self._harness.buf)
            print line

        next.append(self._next)

if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='Publishes memory info.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    args = parser.parse_args()

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    free = MemoryMonitor(dc)

    while True:
        now = time.time()
        next = [now + 10.0]
        pollin = []
        pollout = []
        pollerr = []
        dc.Poll(now, next, pollin, pollout, pollerr)
        free.Poll(now, next, pollin, pollout, pollerr)
        timeout = min(next) - time.time()
        if timeout > 0:
            zmq.select(pollin, pollout, pollerr, timeout = timeout)
