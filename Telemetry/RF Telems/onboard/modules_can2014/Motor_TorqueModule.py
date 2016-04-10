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

class Motor_TorqueUpdater(object):
	def __init__(self, records, harnesses, torqueFig, torquePlot):
		self.records = records
		self.harnesses = harnesses
		self.torqueFig = torqueFig
		self.torquePlot = torquePlot

		self.t1 = time.clock()
		self.torque1 = 0
		self.torque2 = 0
		self.torque1Vec = [0]
		self.torque2Vec = [0]
		self.t = [0]

		self.updaters = [self.handleTorque1Value,
							self.handleTorque2Value]

		for i in range(len(self.records)):
			self.records[i].value_callback.append(self.updaters[i])
			self.records[i].Subscribe()

		self.updateGraph()

	def handleTorque1Value(self, record):
		self.harnesses[0].buf = buffer(record.value)
		self.torque1 = self.harnesses[0]['trq'].value

	def handleTorque2Value(self, record):
		self.harnesses[1].buf = buffer (record.value)
		self.torque2 = self.harnesses[1]['trq'].value

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1 
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.torque1Vec += [self.torque1]
			self.torque2Vec += [self.torque2]
			if len(self.t) > 80:
				del self.t[0]
				del self.torque1Vec[0]
				del self.torque2Vec[0]
			self.torquePlot.clear()
			self.torquePlot.plot(self.t, self.torque1Vec, 'r-')
			self.torquePlot.plot(self.t, self.torque2Vec, 'g-')
			self.torquePlot.set_xlim([0, self.t[0]])
			self.torquePlot.invert_xaxis()
			self.torqueFig.canvas.draw()

class Motor_TorqueModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.torqueFrame = ttk.LabelFrame(self.parent, text='Torque Graph')
		self.torqueFrame.propagate(0)

		self.torqueFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.torqueFig, master=self.torqueFrame)
		self.canvas.get_tk_widget().pack()
		self.torquePlot = self.torqueFig.add_subplot(111)
		self.torqueFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []
		self.harnesses = []

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['motcmd']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
		elif names == ['motcmd2']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
			self.updaterThread = threading.Thread(target=Motor_TorqueUpdater, args=(self.records, self.harnesses, self.torqueFig, self.torquePlot))
			self.updaterThread.daemon = True 
			self.updaterThread.start()
