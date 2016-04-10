import Tkinter as tk 
import ttk
import threading

import data_bus

from modules import Strat_SOCModule
from modules import Charge_BattTempModule
from modules import Charge_ModuleVoltagesModule
from modules import Charge_AuxPackModule

class ChargeTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		### left column ###
		self.leftColumn = ttk.Panedwindow(self, orient=tk.VERTICAL, width=250)
		self.add(self.leftColumn)

		self.battTempFrame = Charge_BattTempModule.Charge_BattTempModule(self.leftColumn, self.data_bus, self.updaters).battTempFrame 
		self.leftColumn.add(self.battTempFrame)
		self.battTempFrame['height'] = 165

		self.modVoltFrame = Charge_ModuleVoltagesModule.Charge_ModuleVoltagesModule(self.leftColumn, self.data_bus, self.updaters).modVoltagesFrame
		self.leftColumn.add(self.modVoltFrame)
		self.modVoltFrame['height'] = 165

		### center ###
		self.auxPackFrame = Charge_AuxPackModule.Charge_AuxPackModule(self, self.data_bus).auxPackFrame 
		self.add(self.auxPackFrame)
		self.auxPackFrame['width'] = 250

		### right ###
		self.SOCGraph = Strat_SOCModule.Strat_SOCModule(self.parent, self.data_bus, self.updaters)
		self.socGraphFrame = self.SOCGraph.SOCFrame
		self.add(self.socGraphFrame)

	def updateGraphs(self):
		for updater in self.updaters:
			updater()