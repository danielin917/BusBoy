import json
import struct
import time
import zmq

import data_bus
import wire

class Field(object):
    def __init__(self, bitpos, bitsize, fmt,
                 endian="little", polynomial=None, **kwargs):

        self.fmt = fmt
        self.polynomial = polynomial

        if endian == "little":
            self.struct = struct.Struct("<Q")
        elif endian == "big":
            self.struct = struct.Struct(">Q")
        else:
            raise Exception("Unknown endianness %s" % endian)

        self.bitpos = bitpos
        self.bitsize = bitsize
        self.mask = (2**bitsize - 1) << bitpos
        if fmt == "float":
            if self.bitsize == 32:
                self.int_struct = struct.Struct("I")
                self.float_struct = struct.Struct("f")
            elif self.bitsize == 64:
                self.int_struct = struct.Struct("Q")
                self.float_struct = struct.Struct("d")
            else:
                raise Exception("Unsupported float size %i" % self.bitsize)
        elif fmt == "int":
            self.sign_bitmask = 1 << (self.bitsize - 1)
            self.sign_mask = (2**bitsize - 1)
        elif fmt == "uint":
            pass
        else:
            raise Exception("Unsupported format %s" % fmt)

    def GetTypename(self):
        if self.polynomial or self.fmt == "float":
            return "float"
        if self.fmt == "int":
            return "int"
        if self.fmt == "uint":
            return "uint"

    def Unpack(self, buf, offset=0):
        raw = (self.struct.unpack_from(buf, offset)[0] & self.mask) >> self.bitpos
        if self.fmt == "uint":
            unscaled = raw
        elif self.fmt == "int":
            # If the sign bit is set, turn it into a negative number.
            if raw & self.sign_bitmask:
                unscaled = -(self.sign_mask - raw + 1)
            else:
                unscaled = raw
        elif self.fmt == "float":
            unscaled = self.float_struct.unpack(self.int_struct.pack(raw))[0]
        else:
            raise Exception("Unsupported format %s" % fmt)

        if not self.polynomial:
            return unscaled

        x = 1
        scaled = 0
        for c in self.polynomial:
            scaled += c * x
            x *= unscaled
        return scaled

    def Pack(self, buf, offset, value):
        if self.polynomial:
            unscaled = value - self.polynomial[0]
            l = len(self.polynomial)
            if l > 1:
                unscaled /= self.polynomial[1]
            if l > 2:
                raise Exception("Polynomial too big for packing")
        else:
            unscaled = value

        if fmt == "uint" or fmt == "int":
            raw = unscaled
        elif fmt == "float":
            raw = self.int_struct.unpack(self.float_struct.pack(raw))[0]
        else:
            raise Exception("Unsupported format %s" % fmt)

        combined = self.struct.unpack_from(buf, offset)[0] & ~self.mask
        combined |= (raw << self.bitpos) & self.bitmask
        self.struct.pack_into(buf, offset, combined)


class CanParser(object):
    def __init__(self, databus, config, rx_name, tx_name):
        self.dc = databus
        self.dc.flush_callback.append(self.HandleFlush)
        self.dc.record_callback.append(self.HandleNewRecord)

        self.rx_name = rx_name
        self.tx_name = tx_name

        self.config = []
        files = [f for f in os.listdir(config) if not f.endswith("~")]
        for f in files:
            print "Loading", f
            self.config += json.load(open(os.path.join(config, f), "r"))

        self.HandleFlush()

    def HandleFlush(self):
        # canid indexed records
        self.messages = {}
        for message in self.config:
            identifier = int(message["canid"], 16)
            # Create Fields and metadata for the message.
            fields = {}
            values = {"timestamp":{"typename":"int", "size":8, "offset":0,
                                   "unit":"uS",
                                   "description": "The message's timestamp."}}

            offset = 8
            for name, meta in sorted(message["fields"].items()):
                field = Field(**meta)
                typename = field.GetTypename()
                size = (field.bitsize + 7) / 8
                if size <= 1 and typename != "float":
                    size = 1
                elif size <= 2 and typename != "float":
                    size = 2;
                elif size <= 4:
                    size = 4
                elif size <= 8:
                    size = 8
                else:
                    raise Exception("Field %s size is too big." % name)

                value = {"typename":typename,
                         "size":size,
                         "offset":offset}
                if "unit" in meta:
                    value["unit"] = meta["unit"]
                if "description" in meta:
                    value["description"] = meta["description"]
                offset += size
                fields[name] = field
                values[name] = value

            metadata = {"harness":values}
            if "description" in message:
                metadata["description"] = message["description"]
            # Create the record
            data = "%s\0%s" % (message["name"].encode("utf-8"), json.dumps(metadata))
            record = self.dc.AddRecord(data)

            # And a harness
            record.harness = wire.Harness(values)
            record.can_fields = fields

            self.messages[identifier] = record
            record.canid = identifier
            record.dlc = message["dlc"]

            # We want to receive commands.
            record.value_callback.append(self.HandleTx)
            record.Subscribe()

            self.tx_record = None

    def HandleNewRecord(self, record):
        print record.field
        if record.field.startswith(self.rx_name):
            fields = record.field.split("\0")
            metadata = json.loads(fields[1])
            record.harness = wire.Harness(metadata["harness"])

            record.Subscribe()
            record.value_callback.append(self.HandleRx)

        if record.field.startswith(self.tx_name):
            self.tx_record = record

    def HandleRx(self, rx_record):
        rx_record.harness.buf = buffer(rx_record.value)
        identifier = rx_record.harness.canid.value
        try:
            buf = rx_record.harness.data.value
            record = self.messages[identifier]
            for name, field in record.can_fields.items():
                record.harness[name].value = field.Unpack(buf)
            record.harness.timestamp.value = rx_record.harness.timestamp.value
            record.SetValue(record.harness.buf)
        except KeyError:
            pass

    def HandleTx(self, record):
        if not self.tx_record:
            print "No TX record, ignoring value"
            return
        print "Yes TX record, hanlding value"
        record.harness.buf = record.value
        self.tx_record.harness.canid = record.canid
        self.tx_record.harness.timestamp.value = record.harness.timestamp.value
        self.tx_record.harness.dlc.value = record.dlc
        for name, field in record.fields.items():
            field.Pack(self.tx_record.harness.data.buf, 0, record.harness[name].value)
        self.tx_record.SetValue(self.tx_record.harness.buf)

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

    parser = argparse.ArgumentParser(description='Parses raw CAN messages.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-c', '--config', type=str, default="config/can_parser_2015",
                        help='CAN structure config directory to use')
    parser.add_argument('-r', '--rxname', type=str, default="can.rx",
                        help='Record(s) to receive CAN messages on.')
    parser.add_argument('-t', '--txname', type=str, default="can.tx",
                        help='Record to transmit CAN messages on.')

    args = parser.parse_args()

    if not args.connect:
        raise Exception("Must be connected to at least one data bus.")

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    parser = CanParser(dc, args.config, args.rxname, args.txname)

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
