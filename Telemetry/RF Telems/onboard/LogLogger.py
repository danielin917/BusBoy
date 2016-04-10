import datetime
import time
import json

import data_bus
import wire

class LogLog(object):
	def __init__(self, root, data_bus):
		self.root = root
		self.data_bus = data_bus
		self.date = time.strftime('%c')
		self.LogFile = open('logs/' + self.date, 'w')
		self.data_bus.record_callback.append(self.handleNewRecord)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		record.name = fields[0].replace(".", "_").replace("/", "_").replace("-", "_")
		meta = json.loads(fields[1])
		record.harness = wire.Harness(meta['harness'])
		record.value_callback.append(self.handleValue)
		record.Subscribe()

	def handleValue(self, record):
		record.harness.buf = buffer(record.value)
		packet = " ".join(['%s="%s"' % (key, wire) for key, wire in record.harness.items()])
		print >>self.LogFile, 'time=', time.strftime('%c'), "%f," % time.time(), 'id=', record.name + ',', packet
