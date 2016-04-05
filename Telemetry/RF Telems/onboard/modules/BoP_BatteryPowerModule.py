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

class BoP_BatteryPowerModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.current = 0
		self.voltage = 0
		self.power = 0
		self.powerVec = [0, 0]
		self.t = [0, 0]

		self.batteryPowerFrame = ttk.Labelframe(self.parent, text='Battery Power Graph')
		self.batteryPowerFrame.propagate(0)

		self.batteryPowerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.batteryPowerFig, master=self.batteryPowerFrame)
		self.canvas.get_tk_widget().pack()
		self.batteryPowerPlot = self.batteryPowerFig.add_subplot(111)
		self.powerLine, = self.batteryPowerPlot.plot(self.t, self.powerVec, 'g-')
		self.batteryPowerPlot.set_xlim([0, 300])
		self.batteryPowerPlot.set_ylim([-2500, 2500])
		self.batteryPowerPlot.invert_xaxis()

		self.updatersDict = {'bms_pack_volts' : self.updateVoltage,
							'ab_current' : self.updateCurrent}

		self.harnesses = {}

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def updateVoltage(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		self.voltage = self.harnesses['bms_pack_volts']['pack1'].value
		self.power = self.voltage*self.current

	def updateCurrent(self, record):
		self.harnesses['ab_current'].buf = buffer(record.value)
		self.current = self.harnesses['ab_current']['battery'].value 
		self.power = self.voltage*self.current

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.powerVec += [self.power]
		if self.t[1] > 300:
			del self.t[0]
			del self.powerVec[0]
		self.powerLine.set_xdata(self.t)
		self.powerLine.set_ydata(self.powerVec)
		self.batteryPowerFig.canvas.draw()
	