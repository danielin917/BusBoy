import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class ArrayModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.ArrayFrame = ttk.Labelframe(self.parent, text='Array', width=250)

		self.PowerIn = ttk.Label(self.ArrayFrame, text='Power In: ', font=('Helvetica',36))
		self.PowerIn.grid(column=1, row=1)
		self.PowerInData = ttk.Label(self.ArrayFrame, text='N/A', font=('Helvetica',36), foreground='green', width=4)
		self.PowerInData.grid(column=2, row=1)

		self.Current = ttk.Label(self.ArrayFrame, text='Current: ')
		self.Current.grid(column=3, row=1)
		self.CurrentData = ttk.Label(self.ArrayFrame, text='N/A', width=7)
		self.CurrentData.grid(column=4, row=1)

		self.updatersDict = {'bms_pack_volts' : self.handleVoltage,
							'ab_current' : self.handleCurrent}

		#self.harnesses = {}

		self.currentValue = 0
		self.voltageValue = 0

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def handleVoltage(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		self.voltageValue = self.harnesses['bms_pack_volts']['pack0'].value
		self.PowerInData['text'] = "%.2f" % (self.currentValue*self.voltageValue)


	def handleCurrent(self, record):
		self.harnesses['ab_current'].buf = buffer(record.value)
		self.currentValue = self.harnesses['ab_current']['array'].value 
		self.CurrentData['text'] = "%.2f" % self.currentValue
		self.PowerInData['text'] = "%.2f" % (self.currentValue*self.voltageValue)

