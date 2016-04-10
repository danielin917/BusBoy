import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg
import threading
import numpy as np 

import data_bus
import wire

class MotorPowerUpdater(object):
	def __init__(self, records, harnesses, motorPowerFig, motorPowerPlot):
		self.records = records
		self.harnesses = harnesses
		self.motorPowerFig = motorPowerFig
		self.motorPowerPlot = motorPowerPlot
		self.motorPowerFig.canvas.draw()
		
		self.t1 = time.clock()
		self.power1 = 0
		self.powers1 = [0]
		self.power2 = 0
		self.powers2 = [0]
		self.t = [0]

		self.updaters = [self.handleMot1Value,
							self.handleMot2Value]

		for i in range(len(self.records)):
			self.records[i].value_callback.append(self.updaters[i])
			self.records[i].Subscribe()

		self.updateGraph()

	def handleMot1Value(self, record):   #power from motor 1
		self.harnesses[0].buf = buffer(record.value)
		voltage = self.harnesses[0]['volt'].value
		current = self.harnesses[0]['curr'].value
		self.power1 = voltage*current

	def handleMot2Value(self, record):   #power from motor 2
		self.harnesses[1].buf = buffer(record.value)
		voltage = self.harnesses[1]['volt'].value 
		current = self.harnesses[1]['curr'].value 
		self.power2 = voltage*current

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]  #add dt to all elements
			self.t += [0]
			self.powers1 += [self.power1]
			self.powers2 += [self.power2]
			if len(self.t) > 80:
				del self.t[0]
				del self.powers1[0]
				del self.powers2[0]
			self.motorPowerPlot.clear()
			self.motorPowerPlot.plot(self.t, self.powers1, 'r-')
			self.motorPowerPlot.plot(self.t, self.powers2, 'g-')
			self.motorPowerPlot.set_xlim([0, self.t[0]])
			self.motorPowerPlot.invert_xaxis()
			self.motorPowerFig.canvas.draw()

class Strat_MotorPowerModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.motorPowerFrame = ttk.Labelframe(self.parent, text='Motor Power Graph')
		self.motorPowerFrame.propagate(0)

		self.motorPowerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.motorPowerFig, master=self.motorPowerFrame)
		self.canvas.get_tk_widget().pack()
		self.motorPowerPlot = self.motorPowerFig.add_subplot(111)
		self.motorPowerFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []
		self.harnesses = []

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['motbus']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
		elif names == ['motbus2']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
			self.updaterThread = threading.Thread(target=MotorPowerUpdater, args=(self.records, self.harnesses, self.motorPowerFig, self.motorPowerPlot))
			self.updaterThread.daemon = True
			self.updaterThread.start()