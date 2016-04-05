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

class BattTempUpdater(object):
	def __init__(self, record, harness, battTempFig, battTempPlot):
		self.record = record
		self.harness = harness
		self.battTempFig = battTempFig
		self.battTempPlot = battTempPlot
		self.battTempFig.canvas.draw()
		self.record.value_callback.append(self.handleValue)
		self.record.Subscribe()

		self.t1 = time.clock()
		self.temp = 0
		self.tempVec = [0]
		self.t = [0]
		self.updateGraph()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.temp = self.harness['mean_temp'].value
		
	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.tempVec += [self.temp]
			if len(self.t) > 80:
				del self.t[0]
				del self.tempVec[0]
			self.battTempPlot.clear()
			self.battTempPlot.plot(self.t, self.tempVec, 'r-')
			self.battTempPlot.set_xlim([0, self.t[0]])
			self.battTempPlot.invert_xaxis()
			self.battTempFig.canvas.draw()

class Charge_BattTempModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.battTempFrame = ttk.Labelframe(self.parent, text='Batt Temp Graph')
		self.battTempFrame.propagate(0)

		self.battTempFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.battTempFig, master=self.battTempFrame)
		self.canvas.get_tk_widget().pack()
		self.battTempPlot = self.battTempFig.add_subplot(111)
		self.battTempFig.subplots_adjust(left=.15, bottom=.15, top=.9, right=.95)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=BattTempUpdater, args=(record, harness, self.battTempFig, self.battTempPlot))
			self.updaterThread.daemon = True
			self.updaterThread.start()