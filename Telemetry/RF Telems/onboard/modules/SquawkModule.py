import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class SquawkModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.SquawkFrame = ttk.Labelframe(self.parent, text='Squawk', width=125)

		self.SquawkData = ttk.Label(self.SquawkFrame, text='N/A')
		self.SquawkData.grid()