import json
import socket
import struct
import time
import zmq

import data_bus
import wire


class LegacyBridge(object):
    def __init__(self, databus, sock):
        self.dc = databus
        self.dc.flush_callback.append(self.HandleFlush)
        self.dc.record_callback.append(self.HandleNewRecord)
        self.sock = sock
        self.clients = {}

        self.HandleFlush()

    def HandleFlush(self):
        pass

    def HandleNewRecord(self, record):
        fields = record.field.split("\0")
        if len(fields) <= 1:
            return
        metadata = json.loads(fields[1])
        if not "harness" in metadata:
            return

        record.name = fields[0].replace(".", "_").replace("/", "_").replace("-", "_")
        record.harness = wire.Harness(metadata["harness"])
        record.Subscribe()
        record.value_callback.append(self.HandleRx)

    def HandleRx(self, record):
        record.harness.buf = buffer(record.value)
        packet = " ".join(["data id='%s'" % record.name] + ["%s='%s'" % (key, wire) for key, wire in record.harness.items()])
        for client in self.clients:
            self.sock.sendto(packet, client)

    def Poll(self, now, next, pollin, *args):
        try:
            while True:
                message, client = self.sock.recvfrom(1500)
                if not client in self.clients:
                    print "Client %s:%i connected" % client
                self.clients[client] = now
        except socket.error:
            pass

        valid_clients = {}
        for client, last in self.clients.items():
            if now - last < 10.0:
                valid_clients[client] = last
            else:
                print "Client %s:%i timed out" % client
        self.clients = valid_clients

        pollin.append(self.sock)
        next.append(now + 1.0)

if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='Parses raw CAN messages.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-u', '--udp', type=int, default=9999,
                        help='Base port to listen for clients on.')
    parser.add_argument('--udp_ip', default="0.0.0.0",
                        help='IP address to bind to.')


    args = parser.parse_args()

    if not args.connect:
        raise Exception("Must be connected to at least one data bus.")

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))


    udp_address = (args.udp_ip, args.udp)
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.settimeout(0.0)
    s.bind(udp_address)
    bridge = LegacyBridge(dc, s)

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
