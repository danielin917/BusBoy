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

class Strat_SOCModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.data_bus = data_bus
		self.parent = parent
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.SOCFrame = ttk.Labelframe(self.parent, text='SOC Graph')

		self.SOCFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.SOCFig, master=self.SOCFrame)
		self.canvas.get_tk_widget().pack()
		self.SOCPlot = self.SOCFig.add_subplot(111)
		self.canvas.draw()

		self.minVolts = 0
		self.SOC = 0
		self.beginTime = time.clock()
		self.SOCvec = [0]
		self.t = [0]

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name == ['bms_data']:
			meta = json.loads(fields[1])
			self.harness = wire.Harness(meta['harness'])
			record.value_callback.append(self.handleValue)
			record.Subscribe()
			self.updateGraphThread = threading.Thread(target=self.updateGraph)
			self.updateGraphThread.Daemon = True
			self.updateGraphThread.start()

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.minVolts = self.harness['min_volts'].value 

	def updateGraph(self):
		while True:
			Vmin = self.minVolts
			self.findSOC(Vmin)
			dt = time.clock() - self.beginTime
			self.beginTime += dt 
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.SOCvec += [self.SOC]
			if len(self.t) > 80:
				del self.t[0]
				del self.SOCvec[0]
			self.SOCPlot.clear()
			self.SOCPlot.plot(self.t, self.SOCvec, 'g-')
			self.SOCPlot.set_xlim([0, self.t[0]])
			self.SOCPlot.set_ylim([0, 1])
			self.SOCPlot.invert_xaxis()
			self.canvas.draw()

	def findSOC(self, Vmin):
		self.SOC = .5
		dSOC = .5
		for i in range(16):
			OCV = self.calcOCV()
			if (OCV > Vmin):
				self.SOC -= dSOC
			elif (OCV < Vmin):
				self.SOC += dSOC
			dSOC /= 2
		if self.SOC > 1:
			self.SOC = 1
		elif self.SOC < 0:
			self.SOC = 0

	def calcOCV(self):
		if (self.SOC < 0.002):
			return ((-2047.64*(self.SOC) -275.8693)*(self.SOC) + 29.3204)*(self.SOC) + 2.5
		if (self.SOC < 0.008):
			return ((-2047.64*(self.SOC-.002) -288.1552)*(self.SOC-.002) + 28.1923)*(self.SOC-.002) + 2.5575
		if (self.SOC < 0.017):
			return ((-5166.3*(self.SOC-.008) - 325.0128)*(self.SOC-.008) + 24.5133)*(self.SOC-.008) + 2.7159
		if (self.SOC < 0.025):
			return ((9229.85*(self.SOC-.017) - 464.5040)*(self.SOC-.017) + 17.4077)*(self.SOC-.017) + 2.9064
		if (self.SOC < 0.040):
			return ((2395*(self.SOC-.025) - 242.9874)*(self.SOC-.025) + 11.7477)*(self.SOC-.025) + 3.0206
		if (self.SOC < 0.072):
			return ((1303.4*(self.SOC-.040) -135.1902)*(self.SOC-.040) + 6.0751)*(self.SOC-.040) + 3.1503
		if (self.SOC < 0.1170):
			return ((131.5474*(self.SOC-.072) - 10.0619)*(self.SOC-.072) + 1.4270)*(self.SOC-.072) + 3.2490
		if (self.SOC < 0.1860):
			return ((-70.0483*(self.SOC-.1170) + 7.6980)*(self.SOC-.1170) + 1.3206)*(self.SOC-.1170) + 3.3048
		if (self.SOC < 0.2560):
			return ((24.357*(self.SOC-.1860) - 6.8030)*(self.SOC-.1860) + 1.3823)*(self.SOC-.1860) + 3.4095
		if (self.SOC < 0.3450):
			return ((4.0637*(self.SOC-.2560) - 1.6879)*(self.SOC-.2560) + .7879)*(self.SOC-.2560) + 3.4813
		if (self.SOC < 0.4300):
			return ((3.3408*(self.SOC-.3450) - .6029)*(self.SOC-.3450) + .5840)*(self.SOC-.3450) + 3.5409
		if (self.SOC < 0.5360):
			return ((10.0636*(self.SOC-.4300) + .2490)*(self.SOC-.4300) + .5540)*(self.SOC-.4300) + 3.5883
		if (self.SOC <  0.6090):
			return ((-22.0868*(self.SOC-.5360) + 3.4492)*(self.SOC-.5360) + .9460)*(self.SOC-.5360) + 3.6618
		if (self.SOC < 0.6890):
			return ((1.3250*(self.SOC-.6090) - 1.3878)*(self.SOC-.6090) + 1.0965)*(self.SOC-.6090) + 3.7406
		if (self.SOC < 0.7430):
			return ((10.8857*(self.SOC-.6890) - 1.0698)*(self.SOC-.6890) + .8999)*(self.SOC-.6890) + 3.8201
		if (self.SOC < 0.8140):
			return ((5.9927*(self.SOC-.7430) + .6937)*(self.SOC-.7430) + .8796)*(self.SOC-.7430) + 3.8673
		if (self.SOC <  0.8720):
			return ((4.0614*(self.SOC-.8140) + 1.9702)*(self.SOC-.8140) + 1.0687)*(self.SOC-.8140) + 3.9354
		if (self.SOC < 0.9200):
			return ((-132.9138*(self.SOC-.8720) + 2.6769)*(self.SOC-.8720) + 1.3382)*(self.SOC-.8720) + 4.0048
		if (self.SOC < 0.9770):
			return ((438.7393*(self.SOC-.9200) -16.4627)*(self.SOC-.9200) + .6765)*(self.SOC-.9200) + 4.0605
		if (self.SOC < 0.9890):
			return ((-2349.0578*(self.SOC-.9770) + 58.5617)*(self.SOC-.9770) + 3.0761)*(self.SOC-.9770) + 4.1269
		else:
			return ((-2349.05786*(self.SOC-.9890) -26.0044)*(self.SOC-.9890) + 3.4668)*(self.SOC-.9890) + 4.1681
