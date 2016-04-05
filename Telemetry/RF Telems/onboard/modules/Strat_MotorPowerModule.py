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

class Strat_MotorPowerModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.power1 = 0
		self.powers1 = [0, 0]
		self.power2 = 0
		self.powers2 = [0, 0]
		self.t = [0, 0]

		self.motorPowerFrame = ttk.Labelframe(self.parent, text='Motor Power Graph')
		self.motorPowerFrame.propagate(0)

		self.motorPowerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.motorPowerFig, master=self.motorPowerFrame)
		self.canvas.get_tk_widget().pack()
		self.motorPowerPlot = self.motorPowerFig.add_subplot(111)
		self.power1Line, = self.motorPowerPlot.plot(self.t, self.powers1, 'r-')
		self.power2Line, = self.motorPowerPlot.plot(self.t, self.powers2, 'g-')
		self.resetLine, = self.motorPowerPlot.plot((0, 0), (0, 2000), 'b-')
		self.motorPowerPlot.set_xlim([0, 300])
		self.motorPowerPlot.set_ylim([0,1500])
		self.motorPowerPlot.invert_xaxis()
		self.motorPowerFig.subplots_adjust(bottom=.15, right=.95)

		self.updatersDict = {'motbus' : self.handleMot1Value,
							'motbus2' : self.handleMot2Value}

		self.harnesses = {}

		self.reset = False

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			self.harnesses[name] = wire.Harness(meta['harness'])
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def addResetMark(self):
		self.reset = True
		self.resetTime = 0

	def handleMot1Value(self, record):   #power from motor 1
		self.harnesses['motbus'].buf = buffer(record.value)
		voltage = self.harnesses['motbus']['volt'].value
		current = self.harnesses['motbus']['curr'].value
		self.power1 = voltage*current

	def handleMot2Value(self, record):   #power from motor 2
		self.harnesses['motbus2'].buf = buffer(record.value)
		voltage = self.harnesses['motbus2']['volt'].value 
		current = self.harnesses['motbus2']['curr'].value 
		self.power2 = voltage*current

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]  #add dt to all elements
		self.t += [0]
		self.powers1 += [self.power1]
		self.powers2 += [self.power2]
		if self.t[1] > 300:
			del self.t[0]
			del self.powers1[0]
			del self.powers2[0]
		self.power1Line.set_xdata(self.t)
		self.power1Line.set_ydata(self.powers1)
		self.power2Line.set_xdata(self.t)
		self.power2Line.set_ydata(self.powers2)
		if self.reset:
			self.resetLine.set_xdata((self.resetTime, self.resetTime))
			if self.resetTime > 300:
				self.reset = False
			self.resetTime += dt 
		self.motorPowerFig.canvas.draw()