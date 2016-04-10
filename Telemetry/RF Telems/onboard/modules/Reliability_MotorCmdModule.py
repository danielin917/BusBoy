import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class Reliability_MotorCmd(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.motorCmdFrame = ttk.Labelframe(self.parent, text='Motor Cmd')

		self.resetMotorButton = ttk.Button(self.motorCmdFrame, text='Reset Motor')
		self.resetMotorButton.grid(row=1)

		self.resetBMSButton = ttk.Button(self.motorCmdFrame, text='Reset BMS')
		self.resetBMSButton.grid(row=2)

		self.motorEntry = ttk.Entry(self.motorCmdFrame, width=7)
		self.motorEntry.grid(row=3)

		self.setMotorButton = ttk.Button(self.motorCmdFrame, text='Set Motor')
		self.setMotorButton.grid(row=4)

		self.openRelayButton = ttk.Button(self.motorCmdFrame, text='Open relay')
		self.openRelayButton.grid(row=5)