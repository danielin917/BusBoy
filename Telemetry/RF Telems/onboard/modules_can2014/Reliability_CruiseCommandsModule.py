import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire
import GLOBALS

class Reliability_CruiseCommands(ttk.Frame):
	def __init__(self, root, parent, data_bus, options):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)
		self.options = options

		self.cruiseCommandFrame = ttk.Labelframe(self.parent, text='Cruise Commands')

		self.setSpeed = ttk.Label(self.cruiseCommandFrame, text='Set Speed: ')
		self.setSpeed.grid(row=1, column=1)
		self.setSpeedData = ttk.Label(self.cruiseCommandFrame, text='N/A')
		self.setSpeedData.grid(row=1, column=2)

		self.limit = ttk.Label(self.cruiseCommandFrame, text='Limit: ')
		self.limit.grid(row=2, column=1)
		self.limitData = ttk.Label(self.cruiseCommandFrame, text='N/A')
		self.limitData.grid(row=2, column=2)

		self.speedEntry = ttk.Entry(self.cruiseCommandFrame, width=5)
		self.speedEntry.grid(row=3, column=1)
		self.sendSpeed = ttk.Button(self.cruiseCommandFrame, text='Send Speed')
		self.sendSpeed.grid(row=3, column=2)

		self.limitEntry = ttk.Entry(self.cruiseCommandFrame, width=5)
		self.limitEntry.grid(row=4, column=1)
		self.sendLimit = ttk.Button(self.cruiseCommandFrame, text='Send Limit')
		self.sendLimit.grid(row=4, column=2)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name == ['cruise']:
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.setSpeedData['text'] = "%.2f" % (self.harness['speed'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		self.limitData['text'] = "%.2f" % (self.harness['limit'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])