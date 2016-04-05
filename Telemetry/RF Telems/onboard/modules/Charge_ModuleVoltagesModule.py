import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg
import numpy as np 
import threading

import data_bus
import wire

class Charge_ModuleVoltagesModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.modVoltagesFrame = ttk.Labelframe(self.parent, text='Module Voltages')
		self.modVoltagesFrame.propagate(0)

		self.modules = np.arange(41)
		self.volts = np.zeros(41)

		self.modVoltagesFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.modVoltagesFig, master=self.modVoltagesFrame)
		self.canvas.get_tk_widget().pack()
		self.modVoltagesPlot = self.modVoltagesFig.add_subplot(111)		

		self.harnesses = {}

		self.updatersDict = {'bms_volts_0' : self.voltUpdate0,
							'bms_volts_1' : self.voltUpdate1,
							'bms_volts_2' : self.voltUpdate2,
							'bms_volts_3' : self.voltUpdate3,
							'bms_volts_4' : self.voltUpdate4,
							'bms_volts_5' : self.voltUpdate5,
							'bms_volts_6' : self.voltUpdate6,
							'bms_volts_7' : self.voltUpdate7,
							'bms_volts_8' : self.voltUpdate8,
							'bms_volts_9' : self.voltUpdate9,
							'bms_volts_10' : self.voltUpdate10}

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def updateGraph(self):
		self.modVoltagesPlot.clear()
		self.modVoltagesPlot.bar(self.modules, self.volts)
		self.modVoltagesPlot.set_xlim([0, 41])
		self.modVoltagesPlot.set_ylim([2, 4.5])
		self.modVoltagesFig.canvas.draw()

	def voltUpdate0(self, record):
		self.harnesses['bms_volts_0'].buf = buffer(record.value)
		self.volts[0] = self.harnesses['bms_volts_0']['volt_0'].value 
		self.volts[1] = self.harnesses['bms_volts_0']['volt_1'].value
		self.volts[2] = self.harnesses['bms_volts_0']['volt_2'].value 
		self.volts[3] = self.harnesses['bms_volts_0']['volt_3'].value 

	def voltUpdate1(self, record):
		self.harnesses['bms_volts_1'].buf = buffer(record.value)
		self.volts[4] = self.harnesses['bms_volts_1']['volt_4'].value
		self.volts[5] = self.harnesses['bms_volts_1']['volt_5'].value 
		self.volts[6] = self.harnesses['bms_volts_1']['volt_6'].value 
		self.volts[7] = self.harnesses['bms_volts_1']['volt_7'].value 

	def voltUpdate2(self, record):
		self.harnesses['bms_volts_2'].buf = buffer(record.value)
		self.volts[8] = self.harnesses['bms_volts_2']['volt_8'].value
		self.volts[9] = self.harnesses['bms_volts_2']['volt_9'].value 
		self.volts[10] = self.harnesses['bms_volts_2']['volt_10'].value 
		self.volts[11] = self.harnesses['bms_volts_2']['volt_11'].value 

	def voltUpdate3(self, record):
		self.harnesses['bms_volts_3'].buf = buffer(record.value)
		self.volts[12] = self.harnesses['bms_volts_3']['volt_12'].value 
		self.volts[13] = self.harnesses['bms_volts_3']['volt_13'].value 
		self.volts[14] = self.harnesses['bms_volts_3']['volt_14'].value 
		self.volts[15] = self.harnesses['bms_volts_3']['volt_15'].value

	def voltUpdate4(self, record):
		self.harnesses['bms_volts_4'].buf = buffer(record.value)
		self.volts[16] = self.harnesses['bms_volts_4']['volt_16'].value
		self.volts[17] = self.harnesses['bms_volts_4']['volt_17'].value 
		self.volts[18] = self.harnesses['bms_volts_4']['volt_18'].value 
		self.volts[19] = self.harnesses['bms_volts_4']['volt_19'].value 

	def voltUpdate5(self, record):
		self.harnesses['bms_volts_5'].buf = buffer(record.value)
		self.volts[20] = self.harnesses['bms_volts_5']['volt_20'].value 
		self.volts[21] = self.harnesses['bms_volts_5']['volt_21'].value
		self.volts[22] = self.harnesses['bms_volts_5']['volt_22'].value 
		self.volts[23] = self.harnesses['bms_volts_5']['volt_23'].value 

	def voltUpdate6(self, record):
		self.harnesses['bms_volts_6'].buf = buffer(record.value)
		self.volts[24] = self.harnesses['bms_volts_6']['volt_24'].value 
		self.volts[25] = self.harnesses['bms_volts_6']['volt_25'].value 
		self.volts[26] = self.harnesses['bms_volts_6']['volt_26'].value 
		self.volts[27] = self.harnesses['bms_volts_6']['volt_27'].value 

	def voltUpdate7(self, record):
		self.harnesses['bms_volts_7'].buf = buffer(record.value)
		self.volts[28] = self.harnesses['bms_volts_7']['volt_28'].value 
		self.volts[29] = self.harnesses['bms_volts_7']['volt_29'].value 
		self.volts[30] = self.harnesses['bms_volts_7']['volt_30'].value 
		self.volts[31] = self.harnesses['bms_volts_7']['volt_31'].value 

	def voltUpdate8(self, record):
		self.harnesses['bms_volts_8'].buf = buffer(record.value)
		self.volts[32] = self.harnesses['bms_volts_8']['volt_32'].value 
		self.volts[33] = self.harnesses['bms_volts_8']['volt_33'].value 
		self.volts[34] = self.harnesses['bms_volts_8']['volt_34'].value 
		self.volts[35] = self.harnesses['bms_volts_8']['volt_35'].value 

	def voltUpdate9(self, record):
		self.harnesses['bms_volts_9'].buf = buffer(record.value)
		self.volts[36] = self.harnesses['bms_volts_9']['volt_36'].value 
		self.volts[37] = self.harnesses['bms_volts_9']['volt_37'].value 
		self.volts[38] = self.harnesses['bms_volts_9']['volt_38'].value 
		self.volts[39] = self.harnesses['bms_volts_9']['volt_39'].value 

	def voltUpdate10(self, record):
		self.harnesses['bms_volts_10'].buf = buffer(record.value)
		self.volts[40] = self.harnesses['bms_volts_10']['volt_40'].value
