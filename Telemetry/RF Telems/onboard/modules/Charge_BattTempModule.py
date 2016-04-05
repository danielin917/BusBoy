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

class Charge_BattTempModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.temp = 0
		self.tempVec = [0, 0]
		self.t = [0, 0]

		self.battTempFrame = ttk.Labelframe(self.parent, text='Batt Temp Graph')
		self.battTempFrame.propagate(0)

		self.battTempFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.battTempFig, master=self.battTempFrame)
		self.canvas.get_tk_widget().pack()
		self.battTempPlot = self.battTempFig.add_subplot(111)
		self.tempLine, = self.battTempPlot.plot(self.t, self.tempVec, 'r-')
		self.battTempPlot.set_xlim([0, 300])
		self.battTempPlot.set_ylim([20,50])
		self.battTempPlot.invert_xaxis()
		self.battTempFig.subplots_adjust(left=.15, bottom=.15, top=.9, right=.95)

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'bmstempextremes':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.temp = self.harness['max'].value
		
	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.tempVec += [self.temp]
		if self.t[1] > 300:
			del self.t[0]
			del self.tempVec[0]
		self.tempLine.set_xdata(self.t)
		self.tempLine.set_ydata(self.tempVec)
		self.battTempFig.canvas.draw()