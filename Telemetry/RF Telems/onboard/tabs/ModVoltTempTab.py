import Tkinter as tk 
import ttk
import threading

from modules import Charge_ModuleVoltagesModule
from modules import ModTempModule

class ModVoltTempTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		self.modVoltFrame = Charge_ModuleVoltagesModule.Charge_ModuleVoltagesModule(self, self.data_bus, self.updaters).modVoltagesFrame
		self.add(self.modVoltFrame)
		self.modVoltFrame['width'] = 450

		self.modTempFrame = ModTempModule.ModuleTemperatures(self, self.data_bus, self.updaters).modTempFrame
		self.add(self.modTempFrame)

		self.updaterThread = threading.Thread(target=self.updateGraphs)
		self.updaterThread.Daemon = True
		self.updaterThread.start()

	def updateGraphs(self):
		for updater in self.updaters:
			updater()