import datetime
import time
import json
import Tkinter as tk 
import ttk 

import data_bus
import wire

class Strat_SegIDModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus

		self.segIDFrame = ttk.Labelframe(self.parent, text='Seg ID', height=100)

		self.segID = ttk.Label(self.segIDFrame, text='N/A', font=('Helvetica', 30))
		self.segID.grid()