import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class MPPTModule(ttk.Frame):
	def __init__(self, parent, data_bus, num):
		self.parent = parent
		self.data_bus = data_bus
		self.num = num 
		self.ID = 'MPPT ' + str(self.num)

		self.MPPTFrame = ttk.Labelframe(self.parent, text=self.ID)
		self.MPPTFrame.propagate(0)

		self.Vin = ttk.Label(self.MPPTFrame, text='V in: ', font=('Helvetica', 18))
		self.Vin.grid(row=1, column=1)
		self.VinData = ttk.Label(self.MPPTFrame, text='N/A', font=('Helvetica', 18), width=12)
		self.VinData.grid(row=1, column=2)

		self.Iin = ttk.Label(self.MPPTFrame, text='I in: ', font=('Helvetica', 18))
		self.Iin.grid(row=2, column=1)
		self.IinData = ttk.Label(self.MPPTFrame, text='N/A', font=('Helvetica', 18), width=12)
		self.IinData.grid(row=2, column=2)

		self.Vout = ttk.Label(self.MPPTFrame, text='V out: ', font=('Helvetica', 18))
		self.Vout.grid(row=3, column=1)
		self.VoutData = ttk.Label(self.MPPTFrame, text='N/A', font=('Helvetica', 18), width=12)
		self.VoutData.grid(row=3, column=2)

		self.Pin = ttk.Label(self.MPPTFrame, text='P in: ', font=('Helvetica', 18))
		self.Pin.grid(row=4, column=1)
		self.PinData = ttk.Label(self.MPPTFrame, text='N/A', font=('Helvetica', 18), width=12)
		self.PinData.grid(row=4, column=2)

		self.temp = ttk.Label(self.MPPTFrame, text='Temp: ', font=('Helvetica', 18))
		self.temp.grid(row=5, column=1)
		self.tempData = ttk.Label(self.MPPTFrame, text='N/A', font=('Helvetica', 18), width=12)
		self.tempData.grid(row=5, column=2)