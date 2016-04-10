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

class ModTempUpdater(object):
	def __init__(self, records, harnesses, modTempFig, modTempPlot):
		self.records = records
		self.harnesses = harnesses
		self.modTempFig = modTempFig
		self.modTempPlot = modTempPlot

		self.modules = np.arange(35)
		self.temps = np.zeros(35)

		self.updaters = [self.dataUpdate0,
							self.dataUpdate1,
							self.dataUpdate2,
							self.dataUpdate3,
							self.dataUpdate4,
							self.dataUpdate5,
							self.dataUpdate6,
							self.dataUpdate7,
							self.dataUpdate8]

		for i in range(len(self.records)):
			self.records[i].value_callback.append(self.updaters[i])
			self.records[i].Subscribe()

		self.updateGraph()

	def updateGraph(self):
		while True:
			time.sleep(1)
			self.modTempPlot.clear()
			self.modTempPlot.bar(self.modules, self.temps)
			self.modTempFig.canvas.draw()

	def dataUpdate0(self, record):
		self.harnesses[0].buf = buffer(record.value)
		self.temps[0] = self.harnesses[0]['node0_temp'].value
		self.temps[1] = self.harnesses[0]['node1_temp'].value
		self.temps[2] = self.harnesses[0]['node2_temp'].value
		self.temps[3] = self.harnesses[0]['node3_temp'].value

	def dataUpdate1(self, record):
		self.harnesses[1].buf = buffer(record.value)
		self.temps[4] = self.harnesses[1]['node4_temp'].value 
		self.temps[5] = self.harnesses[1]['node5_temp'].value 
		self.temps[6] = self.harnesses[1]['node6_temp'].value 
		self.temps[7] = self.harnesses[1]['node7_temp'].value 

	def dataUpdate2(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.temps[8] = self.harnesses[2]['node8_temp'].value 
		self.temps[9] = self.harnesses[2]['node9_temp'].value 
		self.temps[10] = self.harnesses[2]['node10_temp'].value 
		self.temps[11] = self.harnesses[2]['node11_temp'].value 

	def dataUpdate3(self, record):
		self.harnesses[3].buf = buffer(record.value)
		self.temps[12] = self.harnesses[3]['node12_temp'].value
		self.temps[13] = self.harnesses[3]['node13_temp'].value
		self.temps[14] = self.harnesses[3]['node14_temp'].value 
		self.temps[15] = self.harnesses[3]['node15_temp'].value 

	def dataUpdate4(self, record):
		self.harnesses[4].buf = buffer(record.value)
		self.temps[16] = self.harnesses[4]['node16_temp'].value
		self.temps[17] = self.harnesses[4]['node17_temp'].value 
		self.temps[18] = self.harnesses[4]['node18_temp'].value
		self.temps[19] = self.harnesses[4]['node19_temp'].value 

	def dataUpdate5(self, record):
		self.harnesses[5].buf = buffer(record.value)
		self.temps[20] = self.harnesses[5]['node20_temp'].value 
		self.temps[21] = self.harnesses[5]['node21_temp'].value 
		self.temps[22] = self.harnesses[5]['node22_temp'].value 
		self.temps[23] = self.harnesses[5]['node23_temp'].value 

	def dataUpdate6(self, record):
		self.harnesses[6].buf = buffer(record.value)
		self.temps[24] = self.harnesses[6]['node24_temp'].value 
		self.temps[25] = self.harnesses[6]['node25_temp'].value 
		self.temps[26] = self.harnesses[6]['node26_temp'].value 
		self.temps[27] = self.harnesses[6]['node27_temp'].value 

	def dataUpdate7(self, record):
		self.harnesses[7].buf = buffer(record.value)
		self.temps[28] = self.harnesses[7]['node28_temp'].value 
		self.temps[29] = self.harnesses[7]['node29_temp'].value 
		self.temps[30] = self.harnesses[7]['node30_temp'].value 
		self.temps[31] = self.harnesses[7]['node31_temp'].value 

	def dataUpdate8(self, record):
		self.harnesses[8].buf = buffer(record.value)
		self.temps[32] = self.harnesses[8]['node32_temp'].value 
		self.temps[33] = self.harnesses[8]['node33_temp'].value 
		self.temps[34] = self.harnesses[8]['node34_temp'].value 
 
class ModuleTemperatures(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.modTempFrame = ttk.Labelframe(self.parent, text='Module Temperatures')
		self.modTempFrame.propagate(0)

		self.modTempFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.modTempFig, master=self.modTempFrame)
		self.canvas.get_tk_widget().pack()
		self.modTempPlot = self.modTempFig.add_subplot(111)

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
		elif name == ['bms_node_data8']:  #this must be the last record
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
			self.updaterThread = threading.Thread(target=ModTempUpdater, args=(self.records, self.harnesses, self.modTempFig, self.modTempPlot))
			self.updaterThread.daemon = True 
			self.updaterThread.start()
