import Tkinter as tk 
import ttk
import threading

import data_bus

from modules import Motor_TorqueModule
from modules import Motor_SpeedModule
from modules import Strat_MotorPowerModule
from modules import Motor_DualMotorModule 

class MotorTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus, options):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options

		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)

		self.leftPane = ttk.Panedwindow(self, orient=tk.VERTICAL)
		self.add(self.leftPane)

		self.leftTopPane = ttk.Panedwindow(self.leftPane, orient=tk.HORIZONTAL, height=160)
		self.leftPane.add(self.leftTopPane)

		self.torqueGraph = Motor_TorqueModule.Motor_TorqueModule(self.leftTopPane, self.data_bus, self.updaters).torqueFrame
		self.leftTopPane.add(self.torqueGraph)
		self.torqueGraph['width'] = 250

		self.speedGraph = Motor_SpeedModule.Motor_SpeedModule(self.leftTopPane, self.data_bus, self.options, self.updaters).speedFrame
		self.leftTopPane.add(self.speedGraph)
		self.speedGraph['width'] = 250

		self.dualMotor = Motor_DualMotorModule.DualMotorModule(self.leftPane, self.data_bus).dualMotorFrame 
		self.leftPane.add(self.dualMotor)

		self.motorPowerGraph2 = Strat_MotorPowerModule.Strat_MotorPowerModule(self, self.data_bus, self.updaters).motorPowerFrame 
		self.add(self.motorPowerGraph2)

	def updateGraphs(self):
		for updater in self.updaters:
			updater()