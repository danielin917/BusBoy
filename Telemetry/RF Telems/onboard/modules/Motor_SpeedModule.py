import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg 
import threading

import data_bus
import wire
import GLOBALS

class Motor_SpeedModule(ttk.Frame):
	def __init__(self, parent, data_bus, options, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options
		self.updaters = updaters

		self.t1 = time.clock()
		self.speed = 0
		self.speedVec = [0, 0]
		self.t = [0, 0]

		self.speedFrame = ttk.Labelframe(self.parent, text='Speed Graph')
		self.speedFrame.propagate(0)

		self.speedFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.speedFig, master=self.speedFrame)
		self.canvas.get_tk_widget().pack()
		self.speedPlot = self.speedFig.add_subplot(111)
		self.speedLine, = self.speedPlot.plot(self.t, self.speedVec, 'g-')
		self.speedPlot.set_xlim([0, 300])
		self.speedPlot.set_ylim([0,130])
		self.speedPlot.invert_xaxis()
		self.speedFig.subplots_adjust(bottom=.15, right=.95)

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'motvel':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.speed = self.harness['vehvel'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()]

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.speedVec += [self.speed]
		if self.t[1] > 300:
			del self.t[0]
			del self.speedVec[0]
		self.speedLine.set_xdata(self.t)
		self.speedLine.set_ydata(self.speedVec)
		self.speedFig.canvas.draw()
			