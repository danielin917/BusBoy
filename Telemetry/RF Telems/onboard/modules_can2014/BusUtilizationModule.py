import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading

import GLOBALS
import data_bus
import wire

class BusUtilizationModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.BusUtilizationFrame = ttk.Labelframe(self.parent, text='Bus Utilization', width=120)

		self.BusUtil = ttk.Label(self.BusUtilizationFrame, text='0.00', font=('Helvetica', 25))
		self.BusUtil.grid(column=1, row=1)

		self.percent = ttk.Label(self.BusUtilizationFrame, text='%', font=('Helvetica', 25))
		self.percent.grid(column=2, row=1)

		self.messages = 0
		self.t1 = time.clock()

		self.busUtilThread = threading.Thread(target=self.calcBusUtil)
		self.busUtilThread.daemon = True
		self.busUtilThread.start()

	def handleNewRecord(self, record):
		record.value_callback.append(self.handleValue)
		record.Subscribe()

	def handleValue(self, record):
		self.messages += 1

	def calcBusUtil(self):
		while True:
			time.sleep(1)
			self.t2 = time.clock()
			dt = self.t2 - self.t1
			busUtilization = self.messages*108/(GLOBALS.MAX_BITS_PER_SECOND*.1*dt)*100
			self.t1 = self.t2
			self.messages = 0
			self.BusUtil['text'] = "%.2f" % busUtilization
			if busUtilization > 70:
				self.BusUtil['foreground'] = 'red'
			else:
				self.BusUtil['foreground'] = 'black'
			