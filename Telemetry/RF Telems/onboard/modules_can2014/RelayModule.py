import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class RelayModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus

		self.RelayFrame = ttk.Labelframe(self.parent, text='Relay', width=120)

		self.data = ttk.Label(self.RelayFrame, text='N/A', font=('Helvetica', 25), width=10)
		self.data.grid()