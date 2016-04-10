import binascii
import struct
import zmq


RECORD_REQUEST_TIMEOUT = 1.0
INITIAL_DOWNSTREAM_TIMEOUT = 10.0
DOWNSTREAM_TIMEOUT = 2.0
HEARTBEAT_PERIOD = 1.0
HEADER_LENGTH = 12

PUBLISH_HIGH_WATER_MARK = 1000
SUBSCRIBE_HIGH_WATER_MARK = 1000
REPLY_HIGH_WATER_MARK = 1000
REQUEST_HIGH_WATER_MARK = 1000


# Message, record and value manipulation helpers.
uint32_t = struct.Struct(">I")

def MakeRecord(index, field, fcs_start):
    out = bytearray(12 + len(field))
    out[0:4] = "REC_"
    out[8:12] = uint32_t.pack(index)
    out[12:] = field
    fcs = binascii.crc32(buffer(out, 8), fcs_start) & 0xffffffff
    uint32_t.pack_into(out, 4, fcs)
    return out

def GetIndex(record, offset=8):
    return uint32_t.unpack_from(record, offset)[0]

def GetFcs(record):
    return uint32_t.unpack_from(record, 4)[0]

def GetHeader(record):
    return buffer(record, 4, 8)

def GetField(record):
    return buffer(record, 12)


# Exceptions
class ConnectionError(Exception):
    def __init__(self, message):
        Exception.__init__(self, message)


# Subclasses
class RecordToken(object):
    def __init__(self, databus, record):
        self._databus = databus
        self._record = record
        self._value = ""
        self.value_callback = []
        self._subscribed = 0

    def Subscribe(self, enable=True):
        header = "VAL_%s" % (self._record[4:12])
        if enable:
            if self._subscribed == 0:
                self._subscribed = True
                self._databus._subscriptions.add(header)
                self._databus._subscribe.setsockopt(zmq.SUBSCRIBE, header)
            self._subscribed += 1
        elif self._subscribed > 0:
            self._subscribed -= 1
            if self._subscribed == 0:
                self._databus._subscriptions.remove(header)
                self._databus._subscribe.setsockopt(zmq.UNSUBSCRIBE, header)

    def Unsubscribe(self):
        return self.Subscribe(False)

    @property
    def field(self):
        return str(GetField(self._record))

    @property
    def value(self):
        return GetField(self._value)

    def SetValue(self, value):
        self._value = bytearray(12 + len(value))
        self._value[0:4] = "VAL_"
        self._value[4:12] = GetHeader(self._record)
        self._value[12:] = value
        if (self._databus._upstream_index is None or
                not self._databus._SendMessageUpstream(self._value)):
            # We can't delegate upstream, so we handle it.
            if self._databus._downstream_address:
                self._databus._SendValueDownstream(self._value)
            if self._subscribed:
                for callback in self.value_callback:
                    callback(self)


class DataBus(object):

    def __init__(self):
        self._records = [RecordToken(self, MakeRecord(0, "v0", 0))]
        self._context = zmq.Context()
        self._upstream_index = None
        self._upstream_addresses = []
        self._downstream_address = None
        self._bootstrapped = False
        self._subscriptions = set()

        self.record_callback = []
        self.bootstrap_callback = []
        self.flush_callback = []

    def Connect(self, address):
        self._upstream_addresses.append(address)
        if self._upstream_index is None:
            self._upstream_index = 0
            self._last_upstream_timestamp = None
            self._upstream_active = False
            self._request_cursor = 0
            self._record_request_start = None
            self._CycleUpstreamConnection()

    def Bind(self, address):
        self._downstream_address = address
        self._publish = self._context.socket(zmq.PUB)
        self._publish.hwm = PUBLISH_HIGH_WATER_MARK
        self._publish.bind("tcp://%s:%i" % self._downstream_address)
        self._reply = self._context.socket(zmq.ROUTER)
        self._reply.hwm = REPLY_HIGH_WATER_MARK
        self._reply.bind("tcp://%s:%i" % (self._downstream_address[0],
                                          self._downstream_address[1] + 1))
        # We need to subscribe to all values to pass along downstream.
        self._subscriptions.add("VAL_")
        if self._upstream_addresses:
            self._subscribe.setsockopt(zmq.SUBSCRIBE, "VAL_")
        self._next_heartbeat = None

    @property
    def bootstrapped(self):
        return self._upstream_index is None or self._bootstrapped

    def Flush(self):
        old_version = GetField(self._records[0]._record)
        self._records = [
            RecordToken(self, MakeRecord(0, "v%i" % (int(old_version[1:]) + 1), 0))]
        if self._upstream_index is not None:
            self._request_cursor = 0
            self._SendMessageUpstream(self._records[0]._record)
        if self._downstream_address:
            self._SendRecordDownstream(self._records[0]._record)

        self._bootstrapped = False
        for callback in self.flush_callback:
            callback()

    def AddRecord(self, field):
        print "New local record %i:%s" % (len(self._records), field)
        record = MakeRecord(len(self._records),
                            field,
                            GetFcs(self._records[-1]._record))
        if self._upstream_index is not None:
            self._SendMessageUpstream(record)
        if self._downstream_address:
            self._SendRecordDownstream(record)
        self.AppendRecord(record)
        return self._records[-1]

    def Poll(self, now, next, pollin, pollout, pollerr):
        if self._downstream_address:
            self._DoUpstreamPoll(now, next, pollin, pollout, pollerr)
        if self._upstream_index is not None:
            self._DoDownstreamPoll(now, next, pollin, pollout, pollerr)
        else:
            if not self._bootstrapped:
                self._bootstrapped = True
                for c in self.bootstrap_callback:
                    c()

    def _DoUpstreamPoll(self, now, next, pollin, pollout, pollerr):
        # Bootstrap the heartbeat timer.
        if self._next_heartbeat is None:
            self._next_heartbeat = now

        # We only service a request over REPLY if there is room to respond.
        while (self._reply.getsockopt(zmq.EVENTS) &
                   (zmq.POLLIN | zmq.POLLOUT)) == (zmq.POLLIN | zmq.POLLOUT):
            sender, message = self._reply.recv_multipart()
            if len(message) < HEADER_LENGTH:
                raise ConnectionError("Received short message.")
            index = GetIndex(message)
            if message.startswith("VAL_"):
                if (self._upstream_index is None or
                        not self._SendMessageUpstream(message)):
                    # We can't delegate this upstream, so deal with it.
                    self._SendValueDownstream(message)
                    if index < len(self._records):
                        token = self._records[index]
                        if GetHeader(message) == GetHeader(token._record):
                            token._value = message
                            if token._subscribed:
                                for callback in token.value_callback:
                                    callback(token)
                        else:
                            print "Dropped invalid value '%s'" % GetField(message)
                    else:
                        print "Dropped future value '%s'" % GetField(message)

            elif message.startswith("REC_"):
                if index == len(self._records):
                    new_record = MakeRecord(len(self._records),
                                            GetField(message),
                                            GetFcs(self._records[-1]._record))
                    if new_record == message:
                        print "Received downstream record %i" % len(self._records)
                        self.AppendRecord(new_record)
                        if self._upstream_index is not None:
                            self._SendMessageUpstream(new_record)
                        self._SendRecordDownstream(new_record)
                    else:
                        print "Received inconsistent downstream record."
                elif index == 0:
                    # Got a version record. We might need to flush.
                    field = GetField(message)
                    if field[0] != 'v' or not field[1:].isdigit():
                        raise ConnectionError(
                            "Received bad version record '%s'" % (GetField(message)))
                    old_version = int(self._records[0].field[1:])
                    new_version = int(field[1:])
                    if new_version > old_version:
                        print "Downstream flushed: %s" % (GetField(message))
                        self._records = [RecordToken(self, message)]
                        if self._upstream_index is not None:
                            self._request_cursor = 0
                            self._SendMessageUpstream(message)
                        self._SendRecordDownstream(message)
                        self._bootstrapped = False
                        for callback in self.flush_callback:
                            callback()
            elif message.startswith("REQ_"):
                endIndex = -1
                if len(message)>= 16:
                    endIndex = GetIndex(message, 12) + 1
                if index < len(self._records):
                    request_matches = (GetHeader(message) ==
                                       GetHeader(self._records[index]._record))
                    if not request_matches:
                        # The requested starting record isn't consistent. Re-send
                        # the starting record.
                        self._reply.send_multipart([sender, self._records[index]._record])
                    # If the request was good, or it was for the version record,
                    # we might as well send everything.
                    if request_matches or index == 0:
                        if endIndex == -1:
                            print "Got request for records after %i" % (index)
                            endIndex = len(self._records)
                        else:
                            print "Got request for records after %i and before %i" % (index, endIndex)
                            if endIndex > len(self._records):
                                endIndex = len(self._records)
                        # The requested starting record looks legit. Send as many records
                        # after it as possible.
                        for i in xrange(index + 1, endIndex):
                            if not self._reply.getsockopt(zmq.EVENTS) & zmq.POLLOUT: break
                            self._reply.send_multipart([sender, self._records[i]._record])
                    else:
                        print "Got invalid request for record %i" % (index)
                        # The requested starting record isn't consistent. Re-send
                        # the starting record.
                        self._reply.send_multipart([sender, self._records[index]._record])
                else:
                    print "Got record request in the future %i" % (index)
            # TODO: Values
            else:
                raise ConnectionError("Received unknown message '%s'." % message[0:4])

        # Send the last record as a heartbeat.
        if now >= self._next_heartbeat:
            self._SendRecordDownstream(self._records[-1]._record)
            print "Heartbeat."

        next.append(self._next_heartbeat)
        pollin.append(self._reply)

    def _DoDownstreamPoll(self, now, next, pollin, pollout, pollerr):
        if self._last_upstream_timestamp is None:
            self._last_upstream_timestamp = now + INITIAL_DOWNSTREAM_TIMEOUT

        if (self._record_request_start is not None and
                        now - self._record_request_start > RECORD_REQUEST_TIMEOUT):
            print "Record request timed out."
            self._record_request_start = None

        if self._subscribe.getsockopt(zmq.EVENTS) & zmq.POLLIN:
            self._last_upstream_timestamp = now
            if not self._upstream_active:
                print "Upstream connected."
                self._upstream_active = True
        elif now - self._last_upstream_timestamp > DOWNSTREAM_TIMEOUT:
            if self._upstream_active:
                print "Upstream timed out."
                self._upstream_active = False
            self._CycleUpstreamConnection()
            self._last_upstream_timestamp = now + INITIAL_DOWNSTREAM_TIMEOUT

        # Handle record requests first. This will potentially make us more
        # consistent in preparation for subscription messages.
        while self._record_request.getsockopt(zmq.EVENTS) & zmq.POLLIN:
            message = self._record_request.recv()
            if len(message) < HEADER_LENGTH:
                raise ConnectionError("Received short message.")
            index = GetIndex(message)
            if not message.startswith("REC_"):
                raise ConnectionError("Got '%s' over record request." % message[0:4])
            if self._HandleUpstreamRecord(now, message, index):
                # The fact that we've received a good record means that a request is
                # still going.
                self._record_request_start = now

        # We shouldn't ever receive anything over the normal request line.
        if self._request.getsockopt(zmq.EVENTS) & zmq.POLLIN:
            raise ConnectionError("Received message on request.")

        while self._subscribe.getsockopt(zmq.EVENTS) & zmq.POLLIN:
            message = self._subscribe.recv()
            if len(message) < HEADER_LENGTH:
                raise ConnectionError("Received short message.")
            index = GetIndex(message)
            if message.startswith("VAL_"):
                if self._downstream_address:
                    self._SendValueDownstream(message)
                if index < len(self._records):
                    token = self._records[index]
                    if GetHeader(message) == GetHeader(token._record):
                        token._value = message
                        for callback in token.value_callback:
                            callback(token)
                    else:
                        print "Dropped invalid value '%s'" % GetField(message)
                else:
                    print "Dropped future value '%s'" % GetField(message)
            elif message.startswith("REC_"):
                if self._HandleUpstreamRecord(now, message, index):
                    last_index = len(self._records) - 1
                    if index >= last_index:
                        # We seem to be fully caught up with the upstream. We allow
                        # new record requests.
                        self._record_request_start = None
                        if index == last_index and not self._bootstrapped:
                            # We seem to be at the end of the upstream records.
                            print "Bootstrapped"
                            self._bootstrapped = True
                            for c in self.bootstrap_callback:
                                c()
                    else:
                        # We seem to be ahead of upstream. Send it a record.
                        print "We are ahead of upstream. Sending record %i" % (index + 1)
                        self._SendMessageUpstream(self._records[index + 1]._record)
            # TODO: Values
            else:
                raise ConnectionError("Received unknown message '%s'." % message[0:4])

        pollin.append(self._record_request)
        pollin.append(self._subscribe)

    def _HandleUpstreamRecord(self, now, message, index):
        if index > len(self._records):
            # We're missing records. Re-request them.
            self._RequestRecords(now)
            return False  # We can't validate the record.

        if index < len(self._records) and self._records[index]._record == message:
            # We have this already. Update the request cursor.
            self._request_cursor = max(self._request_cursor, index)
            return True  # The record was good.

        # This is a new record. Validate it.
        new_record = MakeRecord(
            index,
            GetField(message),
            GetFcs(self._records[index-1]._record))
        if new_record != message:
            if index == 0:
                # Got a version record. We might need to flush.
                field = GetField(message)
                if field[0] != 'v' or not field[1:].isdigit():
                    raise ConnectionError(
                        "Received bad version record '%s'" % (GetField(message)))
                old_version = int(self._records[0].field[1:])
                new_version = int(field[1:])
                if new_version > old_version:
                    print "Upstream flushed: %s" % (GetField(message))
                    self._records = [RecordToken(self, message)]
                    self._request_cursor = 0
                    if self._downstream_address:
                        self._SendRecordDownstream(message)
                    self._bootstrapped = False
                    for callback in self.flush_callback:
                        callback()
                    return True
                if new_version < old_version:
                    print "Upstream is on an old version: %s" % (GetField(message))
                    self._SendMessageUpstream(self._records[0]._record)
                    return False
                raise ConnectionError("Protocol version mismatch.")

            print "Received invalid upstream record."
            if index <= self._request_cursor:
                exponential_backoff = 2 * self._request_cursor - len(self._records)
                self._request_cursor = max(0, min(exponential_backoff, index))
            self._RequestRecords(now)
            return False  # The new record was invalid.

        # The record is valid!
        self._request_cursor = max(self._request_cursor, index)
        print "Received upstream record %i" % index
        if self._downstream_address:
            self._SendRecordDownstream(new_record)
        self.AppendRecord(new_record, index)
        if index + 1 < len(self._records):
            print "Shifting records after %i" % (index)
            subscribed = []
            fcs = GetFcs(new_record)
            for i in xrange(index + 1, len(self._records)):
                token = self._records[i]
                if token._subscribed:
                    subscribed.append(token)
                    token.Subscribe(False)
                uint32_t.pack_into(token._record, 8, i)
                fcs = binascii.crc32(buffer(token._record, 8), fcs) & 0xffffffff
                uint32_t.pack_into(token._record, 4, fcs)
            for token in subscribed:
                token.Subscribe(True)

        return True # The record was so good we added it.

    def _SendMessageUpstream(self, message):
        if (self._upstream_active and
                    self._request.getsockopt(zmq.EVENTS) & zmq.POLLOUT):
            self._request.send(message)
            return True
        return False

    def _SendRecordDownstream(self, message):
        if self._publish.getsockopt(zmq.EVENTS) & zmq.POLLOUT:
            self._publish.send(message)
            self._next_heartbeat = now + HEARTBEAT_PERIOD

    def _SendValueDownstream(self, message):
        if self._publish.getsockopt(zmq.EVENTS) & zmq.POLLOUT:
            self._publish.send(message)

    def _CycleUpstreamConnection(self):
        self._upstream_index = (
            (self._upstream_index + 1) % len(self._upstream_addresses))
        hostname, port = self._upstream_addresses[self._upstream_index]
        print "Connecting to upstream %s:%i-%i" % (hostname, port, port + 1)
        self._subscribe = self._context.socket(zmq.SUB)
        self._subscribe.hwm = SUBSCRIBE_HIGH_WATER_MARK
        self._subscribe.connect("tcp://%s:%i" % (hostname, port))
        self._subscribe.setsockopt(zmq.SUBSCRIBE, "REC_")
        for header in self._subscriptions:
            self._subscribe.setsockopt(zmq.SUBSCRIBE, header)
        self._request = self._context.socket(zmq.DEALER)
        self._request.setsockopt(zmq.LINGER, 0)
        self._request.hwm = REQUEST_HIGH_WATER_MARK
        self._request.connect("tcp://%s:%i" % (hostname, port + 1))
        self._record_request = self._context.socket(zmq.DEALER)
        self._record_request.connect("tcp://%s:%i" % (hostname, port + 1))
        self._record_request.setsockopt(zmq.LINGER, 0)

    def _RequestRecords(self, now):
        if (not self._upstream_active or
                    self._record_request_start is not None or
                not self._record_request.getsockopt(zmq.EVENTS) & zmq.POLLOUT):
            return
        print "Requesting missing records"
        self._record_request_start = now
        hostname, port = self._upstream_addresses[self._upstream_index]
        self._record_request = self._context.socket(zmq.DEALER)
        self._record_request.setsockopt(zmq.LINGER, 0)
        self._record_request.connect("tcp://%s:%i" % (hostname, port + 1))
        self._record_request.send("REQ_%s" %
                                  GetHeader(self._records[self._request_cursor]._record))

    def AppendRecord(self, record, index = None):
        token = RecordToken(self, record)
        if index is None:
            self._records.append(token)
        else:
            self._records.insert(index, token)

        for callback in self.record_callback:
            callback(token)

if __name__ == "__main__":
    import argparse
    import time

    parser = argparse.ArgumentParser(description='Standalone Data Bus.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Upstream Data Buses to connect to.')
    parser.add_argument('-b', '--bind', type=int,
                        help='Base port to listen for downstream Data Buses on.')
    parser.add_argument('--bind_ip', default="0.0.0.0",
                        help='IP address to bind to.')
    args = parser.parse_args()

    dc = DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))
    if args.bind:
        dc.Bind((args.bind_ip, args.bind))

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
