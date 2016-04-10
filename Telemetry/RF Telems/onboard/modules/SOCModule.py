import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire
import GLOBALS
import Strat_SOCModule

class SOCModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.SOCFrame = ttk.Labelframe(self.parent, text='SOC', width=125)

		self.SOCData = ttk.Label(self.SOCFrame, text='N/A', font=('Helvetica', 36), width=5)
		self.SOCData.grid(row=1, column=1)

		self.minVolts = 0

		self.data_bus.record_callback.append(self.handleNewRecord)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'SOC':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.SOCData['text'] = '%.2f' % (100*self.harness['SOC'].value)
