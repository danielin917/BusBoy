import Tkinter as tk 
import ttk
import threading

from modules import Reliability_BusUtilModule
from modules import Reliability_BreakerModule
from modules import Reliability_SteeringModule
from modules import Reliability_WavesculptorModule
from modules import Reliability_BMSMasterModule
from modules import Reliability_AlarmsModule
from modules import Reliability_CruiseCommandsModule
from modules import Reliability_MotorCmdModule

class ReliabilityTab(ttk.Panedwindow):
	def __init__(self, parent, data_bus, options):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options

		self.updaters = []

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.VERTICAL)

		self.topPane = ttk.Panedwindow(self, orient=tk.HORIZONTAL, height=165)
		self.add(self.topPane)

		self.bottomPane = ttk.Panedwindow(self, orient=tk.HORIZONTAL, height=165)
		self.add(self.bottomPane)

		self.Alarms = Reliability_AlarmsModule.Reliability_Alarms(self.bottomPane, self.data_bus)

		### top pane ###
		self.busUtilFrame = Reliability_BusUtilModule.Reliability_BusUtil(self.topPane, self.data_bus, self.updaters).busUtilFrame 
		self.topPane.add(self.busUtilFrame)
		self.busUtilFrame['width'] = 350

		self.breakerFrame = Reliability_BreakerModule.Reliability_Breaker(self.topPane, self.data_bus, self.Alarms, self.updaters).breakerFrame 
		self.topPane.add(self.breakerFrame)
		self.breakerFrame['width'] = 300

		self.steeringFrame = Reliability_SteeringModule.Reliability_Steering(self.topPane, self.data_bus, self.Alarms, self.updaters).steeringFrame 
		self.topPane.add(self.steeringFrame)
		self.steeringFrame['width'] = 300

		### bottom pane ###
		self.waveSculptorFrame = Reliability_WavesculptorModule.Reliability_Wavesculptor(self.bottomPane, self.data_bus, self.Alarms, self.updaters).waveSculptorFrame 
		self.bottomPane.add(self.waveSculptorFrame)
		self.waveSculptorFrame['width'] = 225

		self.BMSMasterFrame = Reliability_BMSMasterModule.Reliability_BMSMaster(self.bottomPane, self.data_bus, self.Alarms, self.updaters).BMSMasterFrame 
		self.bottomPane.add(self.BMSMasterFrame)
		self.BMSMasterFrame['width'] = 225

		self.alarmsFrame = self.Alarms.alarmsFrame 
		self.bottomPane.add(self.alarmsFrame)
		self.alarmsFrame['width'] = 150

		self.cruiseCMDFrame = Reliability_CruiseCommandsModule.Reliability_CruiseCommands(self.bottomPane, self.data_bus, self.options).cruiseCommandFrame 
		self.bottomPane.add(self.cruiseCMDFrame)
		self.cruiseCMDFrame['width'] = 150

	def updateGraphs(self):
		for updater in self.updaters:
			updater()