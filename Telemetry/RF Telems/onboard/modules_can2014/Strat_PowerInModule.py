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

class PowerInUpdater(object):
	def __init__(self, record, harness, powerFig, powerPlot):
		self.record = record
		self.harness = harness
		self.powerFig = powerFig
		self.powerPlot = powerPlot
		self.powerFig.canvas.draw()
		self.record.value_callback.append(self.handleValue)
		self.record.Subscribe()

		self.t1 = time.clock()
		self.power = 0
		self.powers = [0]
		self.t = [0]

		self.updateGraph()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		voltage = self.harness['sum_volts'].value
		current = self.harness['current'].value
		self.power = voltage*current

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt 
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.powers += [self.power]
			if len(self.t) > 80:
				del self.t[0]
				del self.powers[0]
			self.powerPlot.clear()
			self.powerPlot.plot(self.t, self.powers, 'b-')
			self.powerPlot.set_xlim([0, self.t[0]])
			self.powerPlot.invert_xaxis()
			self.powerFig.canvas.draw()

class Strat_PowerInModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.powerInFrame = ttk.Labelframe(self.parent, text='Power In Graph')
		self.powerInFrame.propagate(0)

		self.powerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.powerFig, master=self.powerInFrame)
		self.canvas.get_tk_widget().pack(side=tk.BOTTOM, fill=tk.BOTH, expand=True)
		self.powerPlot = self.powerFig.add_subplot(111)
		self.powerFig.subplots_adjust(left=.15, right=.95, bottom=.15, top=.9)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=PowerInUpdater, args=(record, harness, self.powerFig, self.powerPlot))
			self.updaterThread.daemon = True
			self.updaterThread.start()