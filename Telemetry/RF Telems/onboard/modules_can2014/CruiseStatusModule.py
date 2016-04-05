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

class CruiseUpdater(object):
	def __init__(self, record, harness, setSpeed, limit, grade, options):
		self.record = record
		self.harness = harness
		self.setSpeed = setSpeed
		self.limit = limit
		self.grade = grade
		self.options = options
		self.record.value_callback.append(self.HandleValue)
		self.record.Subscribe()

	def HandleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.setSpeed['text'] = "%.2f" % (self.harness['speed'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		self.limit['text'] = "%.2f" % (self.harness['limit'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])

class CruiseStatusModule(ttk.Frame):
	def __init__(self, root, parent, data_bus, options):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)
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

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['cruise']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=CruiseUpdater, args=(record, 
																				harness, 
																				self.setSpeedData,
																				self.limitData,
																				self.gradeData,
																				self.options))
			self.updaterThread.daemon = True
			self.updaterThread.start()