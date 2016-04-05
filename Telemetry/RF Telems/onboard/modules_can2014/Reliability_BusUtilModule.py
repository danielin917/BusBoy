import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg 
import threading

import GLOBALS
import data_bus
import wire

class Reliability_BusUtil(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.busUtilFrame = ttk.Labelframe(self.parent, text='Recv Bus Utilization')
		self.busUtilFrame.propagate(0)

		self.busUtilFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.busUtilFig, master=self.busUtilFrame)
		self.canvas.get_tk_widget().pack()
		self.busUtilPlot = self.busUtilFig.add_subplot(111)
		self.busUtilFig.subplots_adjust(bottom=.15, right=.95)

		self.messages = 0
		self.t1 = time.clock()
		self.busUtil = [0]
		self.t = [0]

		self.updateThread = threading.Thread(target=self.updateGraph)
		self.updateThread.daemon = True
		self.updateThread.start()
		
		
	def handleNewRecord(self, record):
		record.value_callback.append(self.handleValue)
		record.Subscribe()

	def handleValue(self, record):
		self.messages += 1

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.busUtil += [self.messages*108/(GLOBALS.MAX_BITS_PER_SECOND*.1*dt)*100]  #this formula was copied from the old telem gui and serves as a worst case bus util estimate, maybe refine this later
			self.messages = 0
			if len(self.t) > 80:
				del self.t[0]
				del self.busUtil[0]
			self.busUtilPlot.clear()
			self.busUtilPlot.plot(self.t, self.busUtil, 'c-')
			self.busUtilPlot.set_xlim([0, self.t[0]])
			self.busUtilPlot.invert_xaxis()
			self.canvas.draw()
