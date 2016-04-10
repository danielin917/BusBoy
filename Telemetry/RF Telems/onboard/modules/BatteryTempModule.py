import datetime
import time
import json
import Tkinter as tk 
import ttk
import GLOBALS

import data_bus
import wire

class BatteryTempModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.BatteryTempFrame = ttk.Labelframe(self.parent, text='Battery Temp', width=120)

		self.Temp = ttk.Label(self.BatteryTempFrame, text='N/A', font=('Helvetica', 25))
		self.Temp.grid()

		self.data_bus.record_callback.append(self.HandleNewRecord)

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'bmstempextremes':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.Temp['text'] = '%.2f' % self.harness['max'].value 
		if self.harness['max'].value > GLOBALS.BATT_OVERTEMP_THRESHOLD:
			self.Temp['foreground'] = 'red'
		else:
			self.Temp['foreground'] = 'black'