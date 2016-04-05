import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire
import GLOBALS	

class SpeedModule(ttk.Frame):
	def __init__(self, parent, data_bus, options):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options

		self.speedModuleFrame = ttk.Labelframe(self.parent, text='Speed', width=120)
		self.speedInfo = ttk.Label(self.speedModuleFrame, text='N/A', font=('Helvetica', 28))
		self.speedInfo.grid(column=0, row=0)
		self.speedUnits = ttk.Label(self.speedModuleFrame, text='Km/h', font=('Helvetica', 18))
		self.speedUnits.grid(column=3, row=0)

		self.torqueModuleFrame = ttk.Labelframe(self.parent, text='Torque', width=120)
		self.torque1Info = ttk.Label(self.torqueModuleFrame, text='N/A', font=('Helvetica', 28), width=6, foreground='red')
		self.torque1Info.grid(column=0, row=0)
		self.torque2Info = ttk.Label(self.torqueModuleFrame, text='N/A', font=('Helvetica', 28), width=6, foreground='green')
		self.torque2Info.grid(column=1, row=0)

		self.harnesses = {}

		self.updatersDict = {'motcmd' : self.HandleTrq1Value,
							'motcmd2' : self.HandleTrq2Value,
							'motvel' : self.HandleVelValue}

		self.data_bus.record_callback.append(self.HandleNewRecord)

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			self.harnesses[name] = wire.Harness(meta['harness'])
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def HandleTrq1Value(self, record):
		self.harnesses['motcmd'].buf = buffer(record.value)
		self.torque1Info['text'] = "%.2f" % self.harnesses['motcmd']['trq'].value

	def HandleTrq2Value(self, record):
		self.harnesses['motcmd2'].buf = buffer(record.value)
		self.torque2Info['text'] = "%.2f" % self.harnesses['motcmd2']['trq'].value

	def HandleVelValue(self, record):
		self.harnesses['motvel'].buf = buffer(record.value)
		self.speedInfo['text'] = "%.2f" % (self.harnesses['motvel']['vehvel'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		self.speedUnits['text'] = GLOBALS.SPEED_UNITS[self.options.unitsVar.get()]