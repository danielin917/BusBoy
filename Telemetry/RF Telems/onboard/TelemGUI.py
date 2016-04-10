import matplotlib
matplotlib.use('TkAgg')
import datetime
import json
import pprint
import time
import Tkinter as tk 
import ttk
import threading


import data_bus
import wire

from modules import BatteryTab
from tabs import StrategyTab
from tabs import ChargeTab
from tabs import MotorTab
from tabs import BottomOfPack
from tabs import ModVoltTempTab
from tabs import MPPTTab
from tabs import ReliabilityTab
from tabs import BMSAutoCalTab
from modules import OptionsModule
from modules import Strat_SOCModule
from tabs import DataConsole

class TelemetryGUI(ttk.Frame):
	def __init__(self, root, data_bus, subscribe_all, *args, **kargs):

		ttk.Frame.__init__(self, root, *args, **kargs)

		self.data_bus = data_bus

		self.subscribe_all = subscribe_all

		self.root = root

		self.records = []

		self.tabUpdaters = []

		self.mainPane = ttk.PanedWindow(self, orient=tk.VERTICAL)
		self.mainPane.pack(expand=True, fill=tk.BOTH)

		self.Tabs = ttk.Notebook(self.mainPane)

		self.Options = OptionsModule.Options(self.Tabs)

		self.DataConsole = DataConsole.DataConsole(self.mainPane, self.data_bus, self.Options)

		self.mainPane.add(self.DataConsole)
		self.mainPane.add(self.Tabs)

		self.CreateTabs()

		self.Poll()

		self.pack(expand=True, fill=tk.BOTH)

	def CreateTabs(self):
		self.StrategyTab = StrategyTab.StrategyTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.StrategyTab, text='Strategy')
		self.tabUpdaters += [self.StrategyTab.updateGraphs]
		
		self.ChargeTab = ChargeTab.ChargeTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.ChargeTab, text='Charge')
		self.tabUpdaters += [self.ChargeTab.updateGraphs]

		self.BatteryTab = BatteryTab.BatteryTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.BatteryTab, text='Battery')
		self.tabUpdaters += [self.BatteryTab.updateStuff]

		self.MotorTab = MotorTab.MotorTab(self.Tabs, self.data_bus, self.Options)
		self.Tabs.add(self.MotorTab, text='Motor')
		self.tabUpdaters += [self.MotorTab.updateGraphs]

		self.BoPTab = BottomOfPack.BottomOfPackTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.BoPTab, text='Bottom of Pack')
		self.tabUpdaters += [self.BoPTab.updateGraphs]

		self.MVTTab = ModVoltTempTab.ModVoltTempTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.MVTTab, text='Mod Volt/Temp')
		self.tabUpdaters += [self.MVTTab.updateGraphs]

		self.MPPTTab = MPPTTab.MPPTTab(self.Tabs, self.data_bus)
		self.Tabs.add(self.MPPTTab, text='MPPT')
		self.tabUpdaters += [self.MPPTTab.updateStuff]

		self.RelTab = ReliabilityTab.ReliabilityTab(self.Tabs, self.data_bus, self.Options)
		self.Tabs.add(self.RelTab, text='Reliability')
		self.tabUpdaters += [self.RelTab.updateGraphs]

		self.BMSTab = BMSAutoCalTab.BMSTab(self.Tabs)
		self.Tabs.add(self.BMSTab, text='BMS Auto Cal')
		self.tabUpdaters += [self.BMSTab.updateStuff]

 		self.Tabs.add(self.Options.Options, text='Options')
 		self.tabUpdaters += [self.Options.updateStuff]

 	def updateTab(self):
 		while True:
 			self.tabUpdaters[self.Tabs.index('current')]()

 	def Poll(self):
		self.data_bus.Poll(time.time(), [],[],[],[])
		self.tabUpdaters[self.Tabs.index('current')]()
		self.root.after(20, self.Poll)

if __name__ == "__main__":
	import argparse
	import os
	import sys
	import tkSimpleDialog

	parser = argparse.ArgumentParser(description='Data Bus viewer.')
	parser.add_argument('connect', metavar='hostname:base_port', type=str, nargs='*', help='Data Buses to connect to.')
	parser.add_argument('-a', '--all', type=bool, default=True, help='Subscribe to all values.')
	args = parser.parse_args()

	root = tk.Tk()
	root.geometry("1000x800")
	root.title("Telemetry Gooey")

	#program_directory=sys.path[0]
	#root.tk.call('wm',
	#				'iconphoto',
	#				root._w,
	#tk.PhotoImage(file=os.path.join(program_directory,
    #                                                  "resources",
    #                                                  "view.gif")))
	#root.title("Telemetry Gooey")

	dc = data_bus.DataBus()
	for c in args.connect:
		hostname, port = c.split(":")
		dc.Connect((hostname, int(port)))

	Gui = TelemetryGUI(root, dc, args.all)
	Gui.pack(expand=True, fill=tk.BOTH)

	root.mainloop()