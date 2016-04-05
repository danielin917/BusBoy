import argparse
import sys
import time
import zmq

import data_bus

class Flush(object):
    def __init__(self, dc):
        self.databus = dc
        self.databus.bootstrap_callback.append(self.TriggerFlush)

    def TriggerFlush(self):
        print "Flushing..."
        self.databus.bootstrap_callback.remove(self.TriggerFlush)
        self.databus.Flush()
        self.databus.bootstrap_callback.append(self.Finish)

    def Finish(self):
        print "Flushed."
        sys.exit(0)

parser = argparse.ArgumentParser(description='Flushes a downstream data center.')
parser.add_argument('connect', metavar='hostname:base_port',
                    type=str, nargs='*',
                    help='Data Centers to attempt to flush.')
args = parser.parse_args()

dc = data_bus.DataBus()
for c in args.connect:
    hostname, port = c.split(":")
    dc.Connect((hostname, int(port)))

f = Flush(dc)

while True:
    now = time.time()
    next = [now + 1.0]
    pollin = []
    pollout = []
    pollerr = []
    dc.Poll(now, next, pollin, pollout, pollerr)
    timeout = min(next) - time.time()
    if timeout > 0:
        zmq.select(pollin, pollout, pollerr, timeout = timeout)
