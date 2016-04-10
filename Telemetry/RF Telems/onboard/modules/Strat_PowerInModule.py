import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import numpy as np 
import matplotlib.figure as mpfig
import matplotlib.backends.backend_tkagg as tkagg
import threading

import data_bus
import wire

class Strat_PowerInModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.voltage = 0
		self.current = 0
		self.power = 0
		self.powers = [0, 0]
		self.t = [0, 0]

		self.powerInFrame = ttk.Labelframe(self.parent, text='Power In Graph')
		self.powerInFrame.propagate(0)

		self.powerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.powerFig, master=self.powerInFrame)
		self.canvas.get_tk_widget().pack(side=tk.BOTTOM, fill=tk.BOTH, expand=True)
		self.powerPlot = self.powerFig.add_subplot(111)
		self.powerLine, = self.powerPlot.plot(self.t, self.powers, 'b-')
		self.resetLine, = self.powerPlot.plot((0, 0), (0,2500), 'r-')
		self.powerPlot.set_xlim([0, 300])
		self.powerPlot.set_ylim([0, 2500])
		self.powerPlot.invert_xaxis()
		self.powerFig.subplots_adjust(left=.15, right=.95, bottom=.15, top=.9)

		self.harnesses = {}
		self.reset = False

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'bms_pack_volts':
			meta = json.loads(fields[1])
			self.harnesses[name] = wire.Harness(meta['harness'])
			record.value_callback.append(self.updateVoltage)
			record.Subscribe()
		elif name == 'ab_current':
			meta = json.loads(fields[1])
			self.harnesses[name] = wire.Harness(meta['harness'])
			record.value_callback.append(self.updateCurrent)
			record.Subscribe()

	def addResetMark(self):
		self.reset = True
		self.resetTime = 0

	def updateVoltage(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		self.voltage = self.harnesses['bms_pack_volts']['pack1'].value 
		self.power = self.voltage*self.current

	def updateCurrent(self, record):
		self.harnesses['ab_current'].buf = buffer(record.value)
		self.current = self.harnesses['ab_current']['array'].value 
		self.power = self.voltage*self.current

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt 
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.powers += [self.power]
		if self.t[1] > 300:
			del self.t[0]
			del self.powers[0]
		self.powerLine.set_xdata(self.t)
		self.powerLine.set_ydata(self.powers)
		if self.reset:
	 		self.resetLine.set_xdata((self.resetTime, self.resetTime))
			if self.resetTime > 300:
				self.reset = False
			self.resetTime += dt
		self.powerFig.canvas.draw()