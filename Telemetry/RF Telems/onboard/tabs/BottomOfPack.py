import Tkinter as tk 
import ttk
import threading

from modules import BoP_BatteryPowerModule
from modules import BoP_VMinMaxModule
from modules import Strat_MotorPowerModule
from modules import Strat_PowerInModule

class BottomOfPackTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		self.leftPane = ttk.Panedwindow(self, orient=tk.VERTICAL)
		self.add(self.leftPane)

		self.leftTopPane = ttk.Panedwindow(self.leftPane, orient=tk.HORIZONTAL, height=160)
		self.leftPane.add(self.leftTopPane)

		self.powerInFrame = Strat_PowerInModule.Strat_PowerInModule(self.leftTopPane, self.data_bus, self.updaters).powerInFrame 
		self.leftTopPane.add(self.powerInFrame)
		self.powerInFrame['width'] = 250

		self.vMinMaxFrame = BoP_VMinMaxModule.BoP_VMinMaxModule(self.leftTopPane, self.data_bus, self.updaters).vMinMaxFrame 
		self.leftTopPane.add(self.vMinMaxFrame)
		self.vMinMaxFrame['width'] = 250

		self.motorPowerFrame = Strat_MotorPowerModule.Strat_MotorPowerModule(self.leftPane, self.data_bus, self.updaters).motorPowerFrame
		self.leftPane.add(self.motorPowerFrame)

		self.batteryPowerFrame = BoP_BatteryPowerModule.BoP_BatteryPowerModule(self, self.data_bus, self.updaters).batteryPowerFrame
		self.add(self.batteryPowerFrame)

	def updateGraphs(self):
		for updater in self.updaters:
			updater()

