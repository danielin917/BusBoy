import Tkinter as tk 
import ttk
import threading

import data_bus

from modules import Strat_PowerInModule
from modules import Strat_SOCModule
from modules import Strat_MotorPowerModule
from modules import Strat_SolarRadiationModule
from modules import Strat_AnemometerModule
from modules import Strat_SegIDModule
from modules import Strat_StatisticsModule

class StrategyTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		self.column1 = ttk.Panedwindow(self, orient=tk.VERTICAL, width=250)
		self.add(self.column1)

		self.column2 = ttk.Panedwindow(self, orient=tk.VERTICAL, width=250)
		self.add(self.column2)

		self.column3 = ttk.Panedwindow(self, orient=tk.VERTICAL, width=250)
		self.add(self.column3)

		self.column4 = Strat_AnemometerModule.Strat_AnemometerModule(self, self.data_bus).anemometerFrame

		self.add(self.column4)

		self.PinModule = Strat_PowerInModule.Strat_PowerInModule(self.column2, self.data_bus, self.updaters)
		self.SOCModule = Strat_SOCModule.Strat_SOCModule(self.column2, self.data_bus, self.updaters)
		self.PoutModule = Strat_MotorPowerModule.Strat_MotorPowerModule(self.column3, self.data_bus, self.updaters)
		self.RadModule = Strat_SolarRadiationModule.Strat_SolarRadiationModule(self.column3, self.data_bus, self.updaters)

		self.manageCol1()
		self.manageCol2()
		self.manageCol3()

	def manageCol1(self):
		segIDFrame = Strat_SegIDModule.Strat_SegIDModule(self.column1, self.data_bus).segIDFrame
		statisticsFrame = Strat_StatisticsModule.Strat_StatisticsModule(self.column1, self.data_bus, self.SOCModule, self.PinModule, self.PoutModule, self.RadModule).statisticsFrame 
		self.column1.add(segIDFrame)
		self.column1.add(statisticsFrame)
		segIDFrame['height'] = 300
		statisticsFrame['height'] = 100

	def manageCol2(self):
		powerInGraph = self.PinModule.powerInFrame
		self.SOCGraphFrame = self.SOCModule.SOCFrame
		powerInGraph['height'] = 165
		self.SOCGraphFrame['height'] = 165
		self.column2.add(powerInGraph)
		self.column2.add(self.SOCGraphFrame)

	def manageCol3(self):
		motorPowerGraph = self.PoutModule.motorPowerFrame
		solarRadiationGraph = self.RadModule.solarRadiationFrame
		motorPowerGraph['height'] = 165
		solarRadiationGraph['height'] = 165
		self.column3.add(motorPowerGraph)
		self.column3.add(solarRadiationGraph)

	def updateGraphs(self):
		for updateGraph in self.updaters:
			updateGraph()