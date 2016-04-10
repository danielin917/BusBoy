import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class ModulesConnModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus

		self.ModulesConnFrame = ttk.Labelframe(self.parent, text='Modules Conn.?', width=120)

		self.data = ttk.Label(self.ModulesConnFrame, text='N/A', font=('Helvetica', 25))
		self.data.grid()