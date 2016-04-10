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

class BoP_VMinMaxModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.vMin = 0
		self.vMax = 0
		self.vMinVec = [0, 0]
		self.vMaxVec = [0, 0]
		self.t = [0, 0]

		self.vMinMaxFrame = ttk.Labelframe(self.parent, text='V Min/Max Graph')
		self.vMinMaxFrame.propagate(0)

		self.vMinMaxFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.vMinMaxFig, master=self.vMinMaxFrame)
		self.canvas.get_tk_widget().pack()
		self.vMinMaxPlot = self.vMinMaxFig.add_subplot(111)
		self.minLine, = self.vMinMaxPlot.plot(self.t, self.vMinVec, 'r-')
		self.maxLine, = self.vMinMaxPlot.plot(self.t, self.vMaxVec, 'b-')
		self.vMinMaxPlot.set_xlim([0, 300])
		self.vMinMaxPlot.set_ylim([2,4.5])
		self.vMinMaxPlot.invert_xaxis()
		self.vMinMaxFig.subplots_adjust(left=.15, bottom=.15, top=.9, right=.95)

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'bmsvoltextremes':
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.vMin = self.harness['min'].value
		self.vMax = self.harness['max'].value 
		
	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.vMinVec += [self.vMin]
		self.vMaxVec += [self.vMax]
		if self.t[1] > 300:
			del self.t[0]
			del self.vMinVec[0]
			del self.vMaxVec[0]
		self.minLine.set_xdata(self.t)
		self.minLine.set_ydata(self.vMinVec)
		self.maxLine.set_xdata(self.t)
		self.maxLine.set_ydata(self.vMaxVec)
		self.vMinMaxFig.canvas.draw()
