import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class Charge_AuxPackModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus

		self.auxPackFrame = ttk.Labelframe(self.parent, text='Aux Pack')
		self.auxPackFrame.propagate(0)

		self.aux0 = ttk.Label(self.auxPackFrame, text='Aux 0')
		self.aux0.grid(row=1, column=1)
		self.aux1 = ttk.Label(self.auxPackFrame, text='Aux 1')
		self.aux1.grid(row=1, column=3)

		self.aux0Volt = ttk.Label(self.auxPackFrame, text='Volt:')
		self.aux0Volt.grid(row=2, column=1)
		self.aux0VoltData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux0VoltData.grid(row=2, column=2)
		self.aux1Volt = ttk.Label(self.auxPackFrame, text='Volt:')
		self.aux1Volt.grid(row=2, column=3)
		self.aux1VoltData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux1VoltData.grid(row=2, column=4)

		self.aux0Temp = ttk.Label(self.auxPackFrame, text='Temp:')
		self.aux0Temp.grid(row=3, column=1)
		self.aux0TempData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux0TempData.grid(row=3, column=2)
		self.aux1Temp = ttk.Label(self.auxPackFrame, text='Temp:')
		self.aux1Temp.grid(row=3, column=3)
		self.aux1TempData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux1TempData.grid(row=3, column=4)

		self.aux0Error = ttk.Label(self.auxPackFrame, text='Error:')
		self.aux0Error.grid(row=4, column=1)
		self.aux0ErrorData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux0ErrorData.grid(row=4, column=2)
		self.aux1Error = ttk.Label(self.auxPackFrame, text='Error:')
		self.aux1Error.grid(row=4, column=3)
		self.aux1ErrorData = ttk.Label(self.auxPackFrame, text='N/A')
		self.aux1ErrorData.grid(row=4, column=4)