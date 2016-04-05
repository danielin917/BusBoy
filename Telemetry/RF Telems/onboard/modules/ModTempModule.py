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
 
class ModuleTemperatures(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.modTempFrame = ttk.Labelframe(self.parent, text='Module Temperatures')
		self.modTempFrame.propagate(0)

		self.modules = np.arange(41)
		self.temps = np.zeros(41)

		self.modTempFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.modTempFig, master=self.modTempFrame)
		self.canvas.get_tk_widget().pack()
		self.modTempPlot = self.modTempFig.add_subplot(111)		

		self.records = []
		self.harnesses = {}

		self.updatersDict = {'bms_temp_0' : self.dataUpdate0,
							'bms_temp_1' : self.dataUpdate1,
							'bms_temp_2' : self.dataUpdate2,
							'bms_temp_3' : self.dataUpdate3,
							'bms_temp_4' : self.dataUpdate4,
							'bms_temp_5' : self.dataUpdate5,
							'bms_temp_6' : self.dataUpdate6,
							'bms_temp_7' : self.dataUpdate7,
							'bms_temp_8' : self.dataUpdate8,
							'bms_temp_9' : self.dataUpdate9,
							'bms_temp_10' : self.dataUpdate10}

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
		self.modTempPlot.clear()
		self.modTempPlot.bar(self.modules, self.temps)
		self.modTempPlot.set_xlim([0,41])
		self.modTempPlot.set_ylim([20,45])
		self.modTempFig.canvas.draw()

	def dataUpdate0(self, record):
		self.harnesses['bms_temp_0'].buf = buffer(record.value)
		self.temps[0] = self.harnesses['bms_temp_0']['temp_0'].value
		self.temps[1] = self.harnesses['bms_temp_0']['temp_1'].value
		self.temps[2] = self.harnesses['bms_temp_0']['temp_2'].value
		self.temps[3] = self.harnesses['bms_temp_0']['temp_3'].value

	def dataUpdate1(self, record):
		self.harnesses['bms_temp_1'].buf = buffer(record.value)
		self.temps[4] = self.harnesses['bms_temp_1']['temp_4'].value 
		self.temps[5] = self.harnesses['bms_temp_1']['temp_5'].value 
		self.temps[6] = self.harnesses['bms_temp_1']['temp_6'].value 
		self.temps[7] = self.harnesses['bms_temp_1']['temp_7'].value 

	def dataUpdate2(self, record):
		self.harnesses['bms_temp_2'].buf = buffer(record.value)
		self.temps[8] = self.harnesses['bms_temp_2']['temp_8'].value 
		self.temps[9] = self.harnesses['bms_temp_2']['temp_9'].value 
		self.temps[10] = self.harnesses['bms_temp_2']['temp_10'].value 
		self.temps[11] = self.harnesses['bms_temp_2']['temp_11'].value 

	def dataUpdate3(self, record):
		self.harnesses['bms_temp_3'].buf = buffer(record.value)
		self.temps[12] = self.harnesses['bms_temp_3']['temp_12'].value
		self.temps[13] = self.harnesses['bms_temp_3']['temp_13'].value
		self.temps[14] = self.harnesses['bms_temp_3']['temp_14'].value 
		self.temps[15] = self.harnesses['bms_temp_3']['temp_15'].value 

	def dataUpdate4(self, record):
		self.harnesses['bms_temp_4'].buf = buffer(record.value)
		self.temps[16] = self.harnesses['bms_temp_4']['temp_16'].value
		self.temps[17] = self.harnesses['bms_temp_4']['temp_17'].value 
		self.temps[18] = self.harnesses['bms_temp_4']['temp_18'].value
		self.temps[19] = self.harnesses['bms_temp_4']['temp_19'].value 

	def dataUpdate5(self, record):
		self.harnesses['bms_temp_5'].buf = buffer(record.value)
		self.temps[20] = self.harnesses['bms_temp_5']['temp_20'].value 
		self.temps[21] = self.harnesses['bms_temp_5']['temp_21'].value 
		self.temps[22] = self.harnesses['bms_temp_5']['temp_22'].value 
		self.temps[23] = self.harnesses['bms_temp_5']['temp_23'].value 

	def dataUpdate6(self, record):
		self.harnesses['bms_temp_6'].buf = buffer(record.value)
		self.temps[24] = self.harnesses['bms_temp_6']['temp_24'].value 
		self.temps[25] = self.harnesses['bms_temp_6']['temp_25'].value 
		self.temps[26] = self.harnesses['bms_temp_6']['temp_26'].value 
		self.temps[27] = self.harnesses['bms_temp_6']['temp_27'].value 

	def dataUpdate7(self, record):
		self.harnesses['bms_temp_7'].buf = buffer(record.value)
		self.temps[28] = self.harnesses['bms_temp_7']['temp_28'].value 
		self.temps[29] = self.harnesses['bms_temp_7']['temp_29'].value 
		self.temps[30] = self.harnesses['bms_temp_7']['temp_30'].value 
		self.temps[31] = self.harnesses['bms_temp_7']['temp_31'].value 

	def dataUpdate8(self, record):
		self.harnesses['bms_temp_8'].buf = buffer(record.value)
		self.temps[32] = self.harnesses['bms_temp_8']['temp_32'].value 
		self.temps[33] = self.harnesses['bms_temp_8']['temp_33'].value 
		self.temps[34] = self.harnesses['bms_temp_8']['temp_34'].value 
		self.temps[35] = self.harnesses['bms_temp_8']['temp_35'].value 

	def dataUpdate9(self, record):
		self.harnesses['bms_temp_9'].buf = buffer(record.value)
		self.temps[36] = self.harnesses['bms_temp_9']['temp_36'].value 
		self.temps[37] = self.harnesses['bms_temp_9']['temp_37'].value 
		self.temps[38] = self.harnesses['bms_temp_9']['temp_38'].value 
		self.temps[39] = self.harnesses['bms_temp_9']['temp_39'].value 

	def dataUpdate10(self, record):
		self.harnesses['bms_temp_10'].buf = buffer(record.value)
		self.temps[40] = self.harnesses['bms_temp_10']['temp_40'].value
