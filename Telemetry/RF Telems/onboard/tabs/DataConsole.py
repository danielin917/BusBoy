import Tkinter as tk 
import ttk

from modules import SpeedModule
from modules import CruiseStatusModule
from modules import BatteryModule
from modules import ArrayModule
from modules import SOCModule
from modules import SquawkModule
from modules import MotorModule
from modules import MotorCTRLStatusModule
from modules import BusUtilizationModule
from modules import ModulesConnModule
from modules import VoltageStatusModule
from modules import BatteryTempModule
from modules import RelayModule
from modules import BoardErrorModule

class DataConsole(ttk.Panedwindow):
	def __init__(self, parent, data_bus, options):
		self.parent = parent
		self.data_bus = data_bus
		self.options = options

		ttk.Panedwindow.__init__(self, self.parent, orient=tk.HORIZONTAL)
		self.pack(expand=True, fill=tk.BOTH)

		self.leftPane = ttk.Panedwindow(self, orient=tk.VERTICAL, width=650)
		self.leftPane.pack(expand=True, fill=tk.BOTH)
		self.add(self.leftPane)

		self.rightPane = ttk.Panedwindow(self, orient=tk.VERTICAL, width=150)
		self.rightPane.pack(expand=True, fill=tk.BOTH)
		self.add(self.rightPane)

		self.manageLeftPane()
		self.manageRightPane()

	def manageLeftPane(self):
		row1 = ttk.Panedwindow(self.leftPane, orient=tk.HORIZONTAL, height=70)
		row2 = ttk.Panedwindow(self.leftPane, orient=tk.HORIZONTAL, height=120)
		row3 = ttk.Panedwindow(self.leftPane, orient=tk.HORIZONTAL, height=100)
		self.leftPane.add(row1)
		self.leftPane.add(row2)
		self.leftPane.add(row3)

		###row1###
		arrayFrame = ArrayModule.ArrayModule(row1, self.data_bus).ArrayFrame
		arrayFrame.pack()
		SOCFrame = SOCModule.SOCModule(row1, self.data_bus).SOCFrame
		SOCFrame.pack()
		row1.add(arrayFrame)
		row1.add(SOCFrame)

		###row2###
		batteryFrame = BatteryModule.BatteryModule(row2, self.data_bus).BatteryFrame
		batteryFrame.grid(column=1)
		cruiseStatusFrame = CruiseStatusModule.CruiseStatusModule(row2, self.data_bus, self.options).CruiseFrame
		cruiseStatusFrame.grid(column=3)
		row2.add(batteryFrame)
		row2.add(cruiseStatusFrame)

		###row3###
		motorFrame = MotorModule.MotorModule(row3, self.data_bus).MotorFrame
		motorFrame.pack()
		motorCRTLStatusFrame = MotorCTRLStatusModule.MotorCTRLStatusModule(row3, self.data_bus).MotorCTRLStatusFrame
		motorCRTLStatusFrame.pack()
		row3.add(motorFrame)
		row3.add(motorCRTLStatusFrame)

	def manageRightPane(self):
		row1 = ttk.Panedwindow(self.rightPane, orient=tk.HORIZONTAL, height=75)
		row2 = ttk.Panedwindow(self.rightPane, orient=tk.HORIZONTAL, height=75)
		row3 = ttk.Panedwindow(self.rightPane, orient=tk.HORIZONTAL, height=75)
		row4 = ttk.Panedwindow(self.rightPane, orient=tk.HORIZONTAL, height=75)
		self.rightPane.add(row1)
		self.rightPane.add(row2)
		self.rightPane.add(row3)
		self.rightPane.add(row4)

		###row1###
		torqueFrame = SpeedModule.SpeedModule(row1, self.data_bus, self.options).torqueModuleFrame
		torqueFrame.pack()
		speedFrame = SpeedModule.SpeedModule(row1, self.data_bus, self.options).speedModuleFrame
		speedFrame.pack()
		row1.add(torqueFrame)
		row1.add(speedFrame)

		###row2###
		busUtilizationFrame = BusUtilizationModule.BusUtilizationModule(row2, self.data_bus).BusUtilizationFrame
		busUtilizationFrame.pack()
		busUtData = tk.Text(busUtilizationFrame)
		modulesConnFrame = ModulesConnModule.ModulesConnModule(row2, self.data_bus).ModulesConnFrame
		modulesConnFrame.pack()
		modConnData = tk.Text(modulesConnFrame)
		row2.add(busUtilizationFrame)
		row2.add(modulesConnFrame)

		###row3###
		voltageStatusFrame = VoltageStatusModule.VoltageStatusModule(row3, self.data_bus).VoltageStatusFrame
		voltageStatusData = tk.Text(voltageStatusFrame)
		batteryTempFrame = BatteryTempModule.BatteryTempModule(row3, self.data_bus).BatteryTempFrame
		batteryTempData = tk.Text(batteryTempFrame)
		row3.add(voltageStatusFrame)
		row3.add(batteryTempFrame)

		###row4###
		relayFrame = RelayModule.RelayModule(row4, self.data_bus).RelayFrame
		relayData = tk.Text(relayFrame)
		boardErrorFrame = BoardErrorModule.BoardErrorModule(row4, self.data_bus).BoardErrorFrame
		boardErrorData = tk.Text(boardErrorFrame)
		row4.add(relayFrame)
		row4.add(boardErrorFrame)