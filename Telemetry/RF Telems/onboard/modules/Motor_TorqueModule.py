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

class Motor_TorqueModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.t1 = time.clock()
		self.torque1 = 0
		self.torque2 = 0
		self.torque1Vec = [0, 0]
		self.torque2Vec = [0, 0]
		self.t = [0, 0]

		self.torqueFrame = ttk.LabelFrame(self.parent, text='Torque Graph')
		self.torqueFrame.propagate(0)

		self.torqueFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.torqueFig, master=self.torqueFrame)
		self.canvas.get_tk_widget().pack()
		self.torquePlot = self.torqueFig.add_subplot(111)
		self.torque1Line, = self.torquePlot.plot(self.t, self.torque1Vec, 'r-')
		self.torque2Line, = self.torquePlot.plot(self.t, self.torque2Vec, 'g-')
		self.torquePlot.set_xlim([0, 300])
		self.torquePlot.set_ylim([0, 1])
		self.torquePlot.invert_xaxis()
		self.torqueFig.subplots_adjust(bottom=.15, right=.95)

		self.updatersDict = {'motcmd' : self.handleTorque1Value,
							'motcmd2' : self.handleTorque2Value}

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

	def handleTorque1Value(self, record):
		self.harnesses['motcmd'].buf = buffer(record.value)
		self.torque1 = self.harnesses['motcmd']['trq'].value

	def handleTorque2Value(self, record):
		self.harnesses['motcmd2'].buf = buffer(record.value)
		self.torque2 = self.harnesses['motcmd2']['trq'].value

	def updateGraph(self):
		dt = time.clock() - self.t1 
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.torque1Vec += [self.torque1]
		self.torque2Vec += [self.torque2]
		if self.t[1] > 300:
			del self.t[0]
			del self.torque1Vec[0]
			del self.torque2Vec[0]
		self.torque1Line.set_xdata(self.t)
		self.torque1Line.set_ydata(self.torque1Vec)
		self.torque2Line.set_xdata(self.t)
		self.torque2Line.set_ydata(self.torque2Vec)
		self.torqueFig.canvas.draw()
