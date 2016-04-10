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

class BatteryPowerUpdater(object):
	def __init__(self, record, harness, batteryPowerFig, batteryPowerPlot):
		self.record = record
		self.harness = harness
		self.batteryPowerFig = batteryPowerFig
		self.batteryPowerPlot = batteryPowerPlot
		#self.beginTime = time.clock()
		self.record.value_callback.append(self.handleValue)
		self.record.Subscribe()

		self.t1 = time.clock()
		self.power = 0
		self.powerVec = [0]
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
			self.powerVec += [self.power]
			if len(self.t) > 80:
				del self.t[0]
				del self.powerVec[0]
			self.batteryPowerPlot.clear()
			self.batteryPowerPlot.plot(self.t, self.powerVec, 'g-')
			self.batteryPowerPlot.set_xlim([0, self.t[0]])
			self.batteryPowerPlot.invert_xaxis()
			self.batteryPowerFig.canvas.draw()

class BoP_BatteryPowerModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.batteryPowerFrame = ttk.Labelframe(self.root, text='Battery Power Graph')
		self.batteryPowerFrame.propagate(0)

		self.batteryPowerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.batteryPowerFig, master=self.batteryPowerFrame)
		self.canvas.get_tk_widget().pack()
		self.batteryPowerPlot = self.batteryPowerFig.add_subplot(111)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=BatteryPowerUpdater, args=(record, harness, self.batteryPowerFig, self.batteryPowerPlot))
			self.updaterThread.daemon = True
			self.updaterThread.start()
