import Tkinter as tk 
import ttk

from modules import MPPTModule

class MPPTTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		self.MPPT1 = MPPTModule.MPPTModule(self, self.data_bus, 1).MPPTFrame 
		self.add(self.MPPT1)
		self.MPPT1['width'] = 225

		self.MPPT2 = MPPTModule.MPPTModule(self, self.data_bus, 2).MPPTFrame 
		self.add(self.MPPT2)
		self.MPPT2['width'] = 225

		self.MPPT3 = MPPTModule.MPPTModule(self, self.data_bus, 3).MPPTFrame 
		self.add(self.MPPT3)
		self.MPPT3['width'] = 225

		self.MPPT4 = MPPTModule.MPPTModule(self, self.data_bus, 4).MPPTFrame 
		self.add(self.MPPT4)
		self.MPPT3['width'] = 225

	def updateStuff(self):
		pass