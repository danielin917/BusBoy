import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class Strat_StatisticsModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus

		self.statisticsFrame = ttk.Labelframe(self.parent, text='Statistics')

		self.realData = ttk.Label(self.statisticsFrame, text='Real Data')
		self.realData.grid(row=1, column=1, sticky=tk.W)
		self.simulatedData = ttk.Label(self.statisticsFrame, text='Simulated Data')
		self.simulatedData.grid(row=1, column=3, sticky=tk.E)

		self.realSOCData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realSOCData.grid(row=2, column=1)
		self.realSOC = ttk.Label(self.statisticsFrame, text='SOC')
		self.realSOC.grid(row=2, column=2)
		self.simSOCData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simSOCData.grid(row=2, column=3)
		self.simSOC = ttk.Label(self.statisticsFrame, text='SOC')
		self.simSOC.grid(row=2, column=4)

		self.realPinData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realPinData.grid(row=3, column=1)
		self.realPin = ttk.Label(self.statisticsFrame, text='Pin')
		self.realPin.grid(row=3, column=2)
		self.simPinData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simPinData.grid(row=3, column=3)
		self.simPin = ttk.Label(self.statisticsFrame, text='Pin')
		self.simPin.grid(row=3, column=4)

		self.realPoutData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realPoutData.grid(row=4, column=1)
		self.realPout = ttk.Label(self.statisticsFrame, text='Pout')
		self.realPout.grid(row=4, column=2)
		self.simPoutData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simPoutData.grid(row=4, column=3)
		self.simPout = ttk.Label(self.statisticsFrame, text='Pout')
		self.simPout.grid(row=4, column=4)

		self.realRadData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realRadData.grid(row=5, column=1)
		self.realRad = ttk.Label(self.statisticsFrame, text='Rad')
		self.realRad.grid(row=5, column=2)
		self.simRadData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simRadData.grid(row=5, column=3)
		self.simRad = ttk.Label(self.statisticsFrame, text='Rad')
		self.simRad.grid(row=5, column=4)

		self.resetButton = ttk.Button(self.statisticsFrame, text='Reset Avgs')
		self.resetButton.grid(row=6, column=2, columnspan=2)