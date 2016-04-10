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

class ModVoltsUpdater(object):
	def __init__(self, records, harnesses, modVoltagesFig, modVoltagesPlot):
		self.records = records
		self.harnesses = harnesses
		self.modVoltagesFig = modVoltagesFig
		self.modVoltagesPlot = modVoltagesPlot

		self.modules = np.arange(35)
		self.volts = np.zeros(35)

		self.updaters = [self.voltUpdate0,
							self.voltUpdate1,
							self.voltUpdate2,
							self.voltUpdate3,
							self.voltUpdate4,
							self.voltUpdate5,
							self.voltUpdate6,
							self.voltUpdate7,
							self.voltUpdate8]

		for i in range(len(self.records)):
			self.records[i].value_callback.append(self.updaters[i])
			self.records[i].Subscribe()

		self.updateGraph()

	def updateGraph(self):
		while True:
			time.sleep(1)
			self.modVoltagesPlot.clear()
			self.modVoltagesPlot.bar(self.modules, self.volts)
			self.modVoltagesFig.canvas.draw()

	def voltUpdate0(self, record):
		self.harnesses[0].buf = buffer(record.value)
		self.volts[0] = self.harnesses[0]['node0_volts'].value 
		self.volts[1] = self.harnesses[0]['node1_volts'].value
		self.volts[2] = self.harnesses[0]['node2_volts'].value 
		self.volts[3] = self.harnesses[0]['node3_volts'].value 

	def voltUpdate1(self, record):
		self.harnesses[1].buf = buffer(record.value)
		self.volts[4] = self.harnesses[1]['node4_volts'].value
		self.volts[5] = self.harnesses[1]['node5_volts'].value 
		self.volts[6] = self.harnesses[1]['node6_volts'].value 
		self.volts[7] = self.harnesses[1]['node7_volts'].value 

	def voltUpdate2(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.volts[8] = self.harnesses[2]['node8_volts'].value
		self.volts[9] = self.harnesses[2]['node9_volts'].value 
		self.volts[10] = self.harnesses[2]['node10_volts'].value 
		self.volts[11] = self.harnesses[2]['node11_volts'].value 

	def voltUpdate3(self, record):
		self.harnesses[3].buf = buffer(record.value)
		self.volts[12] = self.harnesses[3]['node12_volts'].value 
		self.volts[13] = self.harnesses[3]['node13_volts'].value 
		self.volts[14] = self.harnesses[3]['node14_volts'].value 
		self.volts[15] = self.harnesses[3]['node15_volts'].value

	def voltUpdate4(self, record):
		self.harnesses[4].buf = buffer(record.value)
		self.volts[16] = self.harnesses[4]['node16_volts'].value
		self.volts[17] = self.harnesses[4]['node17_volts'].value 
		self.volts[18] = self.harnesses[4]['node18_volts'].value 
		self.volts[19] = self.harnesses[4]['node19_volts'].value 

	def voltUpdate5(self, record):
		self.harnesses[5].buf = buffer(record.value)
		self.volts[20] = self.harnesses[5]['node20_volts'].value 
		self.volts[21] = self.harnesses[5]['node21_volts'].value
		self.volts[22] = self.harnesses[5]['node22_volts'].value 
		self.volts[23] = self.harnesses[5]['node23_volts'].value 

	def voltUpdate6(self, record):
		self.harnesses[6].buf = buffer(record.value)
		self.volts[24] = self.harnesses[6]['node24_volts'].value 
		self.volts[25] = self.harnesses[6]['node25_volts'].value 
		self.volts[26] = self.harnesses[6]['node26_volts'].value 
		self.volts[27] = self.harnesses[6]['node27_volts'].value 

	def voltUpdate7(self, record):
		self.harnesses[7].buf = buffer(record.value)
		self.volts[28] = self.harnesses[7]['node28_volts'].value 
		self.volts[29] = self.harnesses[7]['node29_volts'].value 
		self.volts[30] = self.harnesses[7]['node30_volts'].value 
		self.volts[31] = self.harnesses[7]['node31_volts'].value 

	def voltUpdate8(self, record):
		self.harnesses[8].buf = buffer(record.value)
		self.volts[32] = self.harnesses[8]['node32_volts'].value 
		self.volts[33] = self.harnesses[8]['node33_volts'].value 
		self.volts[34] = self.harnesses[8]['node34_volts'].value 

class Charge_ModuleVoltagesModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.modVoltagesFrame = ttk.Labelframe(self.parent, text='Module Voltages')
		self.modVoltagesFrame.propagate(0)

		self.modVoltagesFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.modVoltagesFig, master=self.modVoltagesFrame)
		self.canvas.get_tk_widget().pack()
		self.modVoltagesPlot = self.modVoltagesFig.add_subplot(111)

		self.records = []
		self.harnesses = []

		self.names = [['bms_node_data0'],
						['bms_node_data1'],
						['bms_node_data2'],
						['bms_node_data3'],
						['bms_node_data4'],
						['bms_node_data5'],
						['bms_node_data6'],
						['bms_node_data7']]

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
		elif name == ['bms_node_data8']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
			self.updaterThread = threading.Thread(target=ModVoltsUpdater, args=(self.records, self.harnesses, self.modVoltagesFig, self.modVoltagesPlot))
			self.updaterThread.daemon = True 
			self.updaterThread.start()
