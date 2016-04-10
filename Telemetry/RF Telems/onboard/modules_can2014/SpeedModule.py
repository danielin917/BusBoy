import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading

import data_bus
import wire
import GLOBALS

class SpeedUpdater(object):
	def __init__(self, records, harnesses, speed, speedUnits, torque1, torque2, options):
		self.records = records
		self.harnesses = harnesses
		self.speed = speed
		self.speedUnits = speedUnits
		self.torque1 = torque1
		self.torque2 = torque2
		self.options = options
		self.updaters = [self.HandleTrq1Value,
							self.HandleTrq2Value,
							self.HandleVelValue]
		for i in range(len(self.records)):
			self.records[i].value_callback.append(self.updaters[i])
			self.records[i].Subscribe()

	def HandleTrq1Value(self, record):
		self.harnesses[0].buf = buffer(record.value)
		#self.speed['text'] = "%.2f" % (self.harness['vel'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		#self.speedUnits['text'] = GLOBALS.SPEED_UNITS[self.options.unitsVar.get()]
		self.torque1['text'] = "%.2f" % self.harnesses[0]['trq'].value

	def HandleTrq2Value(self, record):
		self.harnesses[1].buf = buffer(record.value)
		self.torque2['text'] = "%.2f" % self.harnesses[1]['trq'].value

	def HandleVelValue(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.speed['text'] = "%.2f" % (self.harnesses[2]['vehvel'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()])
		self.speedUnits['text'] = GLOBALS.SPEED_UNITS[self.options.unitsVar.get()]

class SpeedModule(ttk.Frame):
	def __init__(self, root, parent, data_bus, options):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)
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

		self.records = []
		self.harnesses = []

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['motcmd']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
			#self.updaterThread.daemon = True 
			#self.updaterThread.start()
		elif names == ['motcmd2']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
		elif names == ['motvel']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
			self.updater = SpeedUpdater(self.records, self.harnesses, self.speedInfo, self.speedUnits, self.torque1Info, self.torque2Info, self.options)