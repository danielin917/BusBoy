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

class Motor_SpeedUpdater(object):
	def __init__(self, record, harness, speedFig, speedPlot, options):
		self.record = record
		self.harness = harness
		self.speedFig = speedFig
		self.speedPlot = speedPlot
		self.options = options
		self.record.value_callback.append(self.handleValue)
		self.record.Subscribe()

		self.t1 = time.clock()
		self.speed = 0
		self.speedVec = [0]
		self.t = [0]
		self.updateGraph()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.speed = self.harness['vehvel'].value*GLOBALS.SPEED_UNITS_MULTIPLIER[self.options.unitsVar.get()]

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.speedVec += [self.speed]
			if len(self.t) > 80:
				del self.t[0]
				del self.speedVec[0]
			self.speedPlot.clear()
			self.speedPlot.plot(self.t, self.speedVec, 'g-')
			self.speedPlot.set_xlim([0, self.t[0]])
			self.speedPlot.invert_xaxis()
			self.speedFig.canvas.draw()

class Motor_SpeedModule(ttk.Frame):
	def __init__(self, root, parent, data_bus, options):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)
		self.options = options

		self.speedFrame = ttk.Labelframe(self.parent, text='Speed Graph')
		self.speedFrame.propagate(0)

		self.speedFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.speedFig, master=self.speedFrame)
		self.canvas.get_tk_widget().pack()
		self.speedPlot = self.speedFig.add_subplot(111)
		self.speedFig.subplots_adjust(bottom=.15, right=.95)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['motvel']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=Motor_SpeedUpdater, args=(record, harness, self.speedFig, self.speedPlot, self.options))
			self.updaterThread.daemon = True 
			self.updaterThread.start()
			