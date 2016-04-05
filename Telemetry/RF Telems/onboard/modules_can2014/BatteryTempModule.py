import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading
import GLOBALS

import data_bus
import wire

class BatteryTempUpdater(object):
	def __init__(self, record, harness, Temp):
		self.record = record
		self.harness = harness 
		self.Temp = Temp
		self.record.value_callback.append(self.HandleValue)
		self.record.Subscribe()

	def HandleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.Temp['text'] = "%.2f" % self.harness['mean_temp'].value
		if self.harness['mean_temp'].value > GLOBALS.BATT_OVERTEMP_THRESHOLD:
			self.Temp['foreground'] = 'red'
		else:
			self.Temp['foreground'] = 'black'

class BatteryTempModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)

		self.BatteryTempFrame = ttk.Labelframe(self.parent, text='Battery Temp', width=120)

		self.Temp = ttk.Label(self.BatteryTempFrame, text='N/A', font=('Helvetica', 25))
		self.Temp.grid()

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=BatteryTempUpdater, args=(record, harness, self.Temp))
			self.updaterThread.daemon = True
			self.updaterThread.start()