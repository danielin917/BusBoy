import struct

STRUCT_FORMAT = {
    "int": {1:"b", 2:"h", 4:"i", 8:"q"},
    "uint": {1:"B", 2:"H", 4:"I", 8:"Q"},
    "float": {4:"f", 8:"d"}
}

LITTLE_ENDIAN = "<"
BIG_ENDIAN = ">"

class BytesType(object):
    def __init__(self, size):
        self.size = size
    def Unpack(self, buf, offset=0):
        return bytearray(buffer(buf, offset, self.size))
    def Pack(self, buf, offset, value):
        buf[offset:offset + self.size] = value
    def PrettyPrint(self, buf, offset=0):
        return str(buffer(buf, offset, self.size)).encode("hex")

class StringType(object):
    def __init__(self, size):
        self.size = size
        self._struct = struct.Struct("%is" % size)
    def Unpack(self, buf, offset=0):
        return self._struct.unpack_from(buf, offset)[0].split('\0')[0]
    def Pack(self, buf, offset, value):
        self._struct.pack_into(buf, offset, value)
    def PrettyPrint(self, buf, offset=0):
        return self.Unpack(buf, offset)

class NumericType(object):
    def __init__(self, typename, size, endian=LITTLE_ENDIAN, printf=None):
        self.size = size
        self._struct = struct.Struct(endian + STRUCT_FORMAT[typename][size])
        self._printf = printf
    def Unpack(self, buf, offset=0):
        return self._struct.unpack_from(buf, offset)[0]
    def Pack(self, buf, offset, value):
        self._struct.pack_into(buf, offset, value)
    def PrettyPrint(self, buf, offset=0):
        value = self.Unpack(buf, offset)
        if self._printf is not None:
            return self._printf % value
        return str(value)

def WireType(typename, size=4, endian=LITTLE_ENDIAN, printf=None, *kargs, **kwargs):
    if typename == "bytes":
        return BytesType(size)
    if typename == "string":
        return StringType(size)
    else:
        return NumericType(typename, size, endian, printf)

class Wire(object):
    def __init__(self, buf=None, unit=None, description=None, *kargs, **kwargs):
        self.unit = unit
        self.description = description
        self.type = WireType(*kargs, **kwargs)
        self.buf = buf
        if self.buf is None:
            self.buf = bytearray(self.type.size)
    @property
    def value(self):
        return self.type.Unpack(self.buf)
    @value.setter
    def value(self, val):
        self.type.Pack(self.buf, 0, val)
    def __repr__(self):
        if self.unit:
            return "%s %s" % (self.value, self.unit)
        return "%s" % self.value
    def __str__(self):
        return self.type.PrettyPrint(self.buf)

class BundledWire(object):
    def __init__(self, harness, offset=0, unit=None, description=None,
                 *kargs, **kwargs):
        self._harness = harness
        self.unit = unit
        self.description = description
        self.type = WireType(*kargs, **kwargs)
        self.offset = offset
    @property
    def buf(self):
        return buffer(self._harness.buf, self.offset)
    @property
    def value(self):
        return self.type.Unpack(self._harness.buf, self.offset)
    @value.setter
    def value(self, val):
        self.type.Pack(self._harness.buf, self.offset, val)
    def __repr__(self):
        if self.unit:
            return "%s %s" % (self.value, self.unit)
        return "%s" % self.value
    def __str__(self):
        return self.type.PrettyPrint(self._harness.buf, self.offset)

class Harness(dict):
    def __init__(self, values, buf=None):
        dict.__init__(self)
        self.buf = buf
        for name, value in values.items():
            self[name] = BundledWire(self, **value)
            setattr(self, name, self[name])
        if self.buf is None:
            biggest = max(self.values(), key=lambda x: x.offset + x.type.size)
            self.buf = bytearray(biggest.offset + biggest.type.size)

if __name__ == "__main__":
    h = Harness({"time":{"typename":"int", "unit":"s"},
                 "speed":{"typename":"float", "offset":4, "unit":"m/s"}})
    h.time.value = 1234
    h.speed.value = 2.0
    print [x for x in h.buf]

    print h.speed
    print h.time
    print h
