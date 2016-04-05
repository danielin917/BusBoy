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
import GLOBALS

class Strat_SOCModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.data_bus = data_bus
		self.parent = parent
		self.updaters = updaters

		self.minVolts = 0
		self.SOC = 0
		self.beginTime = time.clock()
		self.SOCvec = [0, 0]
		self.t = [0, 0]
		self.reset = False

		self.SOCFrame = ttk.Labelframe(self.parent, text='SOC Graph')

		self.SOCFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.SOCFig, master=self.SOCFrame)
		self.canvas.get_tk_widget().pack()
		self.SOCPlot = self.SOCFig.add_subplot(111)
		self.line, = self.SOCPlot.plot(self.t, self.SOCvec, 'g-')
		self.resetLine, = self.SOCPlot.plot((0, 0), (0,1), 'r-')
		self.SOCPlot.set_xlim([0, 300])
		self.SOCPlot.set_ylim([0, 1])
		self.SOCPlot.invert_xaxis()
		self.canvas.draw()

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

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
		self.SOC = self.harness['SOC'].value

	def addResetMark(self):
		self.reset = True
		self.resetTime = 0

	def updateGraph(self):
		dt = time.clock() - self.beginTime
		self.beginTime += dt 
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.SOCvec += [self.SOC]
		if self.t[1] > 300:
			del self.t[0]
			del self.SOCvec[0]
		self.line.set_xdata(self.t)
		self.line.set_ydata(self.SOCvec)
		if self.reset:
			self.resetLine.set_xdata((self.resetTime, self.resetTime))
			if self.resetTime > 300:
				self.reset = False
			self.resetTime += dt 
		self.canvas.draw()
