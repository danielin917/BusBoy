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

class VMinMaxUpdater(object):
	def __init__(self, record, harness, vMinMaxFig, vMinMaxPlot):
		self.record = record
		self.harness = harness
		self.vMinMaxFig = vMinMaxFig
		self.vMinMaxPlot = vMinMaxPlot
		self.record.value_callback.append(self.handleValue)
		self.record.Subscribe()

		self.t1 = time.clock()
		self.vMin = 0
		self.vMax = 0
		self.vMinVec = [0]
		self.vMaxVec = [0]
		self.t = [0]
		self.updateGraph()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.vMin = self.harness['min_volts'].value
		self.vMax = self.harness['max_volts'].value 
		
	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.vMinVec += [self.vMin]
			self.vMaxVec += [self.vMax]
			if len(self.t) > 80:
				del self.t[0]
				del self.vMinVec[0]
				del self.vMaxVec[0]
			self.vMinMaxPlot.clear()
			self.vMinMaxPlot.plot(self.t, self.vMinVec, 'r-')
			self.vMinMaxPlot.plot(self.t, self.vMaxVec, 'b-')
			self.vMinMaxPlot.set_xlim([0, self.t[0]])
			self.vMinMaxPlot.invert_xaxis()
			self.vMinMaxFig.canvas.draw()

class BoP_VMinMaxModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.vMinMaxFrame = ttk.Labelframe(self.root, text='V Min/Max Graph')
		self.vMinMaxFrame.propagate(0)

		self.vMinMaxFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.vMinMaxFig, master=self.vMinMaxFrame)
		self.canvas.get_tk_widget().pack()
		self.vMinMaxPlot = self.vMinMaxFig.add_subplot(111)
		self.vMinMaxFig.subplots_adjust(left=.15, bottom=.15, top=.9, right=.95)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=VMinMaxUpdater, args=(record, harness, self.vMinMaxFig, self.vMinMaxPlot))
			self.updaterThread.daemon = True
			self.updaterThread.start()
