import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class VoltageStatusModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.VoltageStatusFrame = ttk.Labelframe(self.parent, text='Voltage Status', width=120)

		self.Status = ttk.Label(self.VoltageStatusFrame, text='N/A', font=('Helvetica', 25), width=10)
		self.Status.grid()