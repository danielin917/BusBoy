import datetime
import time
import Tkinter as tk 
import ttk
import json
import threading

import data_bus
import wire
import OptionsModule
import GLOBALS

class CruiseStatusModule(ttk.Frame):
	def __init__(self, parent, data_bus, options):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options

		self.CruiseFrame = ttk.Labelframe(self.parent, text='Cruise Status')#, width=100)

		self.setSpeed = ttk.Label(self.CruiseFrame, text='Set Speed: ', font=('Helvetica', 18))
		self.setSpeed.grid(column=1, row=1)
		self.setSpeedData = ttk.Label(self.CruiseFrame, text='N/A', font=('Helvetica', 18))
		self.setSpeedData.grid(column=2, row=1)

		self.limit = ttk.Label(self.CruiseFrame, text='Limit')
		self.limit.grid(column=1, row=2)
		self.limitData = ttk.Label(self.CruiseFrame, text='N/A')
		self.limitData.grid(column=2, row=2)

		self.grade = ttk.Label(self.CruiseFrame, text='Grade')
		self.grade.grid(column=1, row=3)
		self.gradeData= ttk.Label(self.CruiseFrame, text='N/A')
		self.gradeData.grid(column=2, row=3)

		self.data_bus.record_callback.append(self.handleNewRecord)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'cruise':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.setSpeedData['text'] = "%.2f" % (self.harness['speed'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		self.gradeData['text'] = '%.2f' % self.harness['grade'].value
