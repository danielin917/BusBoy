import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class BoardErrorModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.BoardErrorFrame = ttk.Labelframe(self.parent, text='Board Error', width=120)

		self.Error = ttk.Label(self.BoardErrorFrame, text='N/A', font=('Helvetica', 25))
		self.Error.grid()