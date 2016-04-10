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
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.messages = 0
		self.t1 = time.clock()
		self.busUtil = [0, 0]
		self.t = [0, 0]

		self.busUtilFrame = ttk.Labelframe(self.parent, text='Recv Bus Utilization')
		self.busUtilFrame.propagate(0)

		self.busUtilFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.busUtilFig, master=self.busUtilFrame)
		self.canvas.get_tk_widget().pack()
		self.busUtilPlot = self.busUtilFig.add_subplot(111)
		self.busUtilLine, = self.busUtilPlot.plot(self.t, self.busUtil, 'c-')
		self.busUtilPlot.set_xlim([0, 300])
		self.busUtilPlot.set_ylim([0, 200])
		self.busUtilPlot.invert_xaxis()
		self.busUtilFig.subplots_adjust(bottom=.15, right=.95)

		self.data_bus.record_callback.append(self.handleNewRecord)

		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		record.value_callback.append(self.handleValue)
		record.Subscribe()

	def handleValue(self, record):
		self.messages += 1

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.busUtil += [self.messages*108/(GLOBALS.MAX_BITS_PER_SECOND*.1*dt)*100]  #this formula was copied from the old telem gui and serves as a worst case bus util estimate, maybe refine this later
		self.messages = 0
		if self.t[1] > 300:
			del self.t[0]
			del self.busUtil[0]
		self.busUtilLine.set_xdata(self.t)
		self.busUtilLine.set_ydata(self.busUtil)
		self.canvas.draw()