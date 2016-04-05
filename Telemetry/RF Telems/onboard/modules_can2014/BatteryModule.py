import datetime
import time
import json
import Tkinter as tk 
import ttk
import numpy as np 
import GLOBALS

import data_bus
import wire

class BatteryModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)

		self.BatteryFrame = ttk.Labelframe(self.parent, text='Battery')#, width=400)

		self.PowerIn = ttk.Label(self.BatteryFrame, text='Power In:', font=('Helvetica', 25))
		self.PowerIn.grid(column=1, row=1)
		self.PowerInData = ttk.Label(self.BatteryFrame, text='N/A', font=('Helvetica', 25), foreground='green')
		self.PowerInData.grid(column=2, row=1)

		self.CurrentBMS = ttk.Label(self.BatteryFrame, text='Current (BMS):')
		self.CurrentBMS.grid(column=1, row=2)
		self.CurrentBMSData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.CurrentBMSData.grid(column=2, row=2)

		self.CurrentCS = ttk.Label(self.BatteryFrame, text='Current (CS):')
		self.CurrentCS.grid(column=1, row=3)
		self.CurrentCSData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.CurrentCSData.grid(column=2, row=3)

		self.Voltage = ttk.Label(self.BatteryFrame, text='Voltage:')
		self.Voltage.grid(column=3, row=1)
		self.VoltageData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.VoltageData.grid(column=4, row=1)

		self.CellVMax = ttk.Label(self.BatteryFrame, text='Cell VMax:')
		self.CellVMax.grid(column=3, row=2)
		self.CellVMaxData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.CellVMaxData.grid(column=4, row=2)

		self.CellVMin = ttk.Label(self.BatteryFrame, text='Cell VMin:')
		self.CellVMin.grid(column=3, row=3)
		self.CellVMinData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.CellVMinData.grid(column=4, row=3)

		self.Temp = ttk.Label(self.BatteryFrame, text='Temp:')
		self.Temp.grid(column=5, row=4)
		self.TempData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.TempData.grid(column=6, row=4)

		self.HighModule = ttk.Label(self.BatteryFrame, text='High Module:')
		self.HighModule.grid(column=5, row=2)
		self.HighModuleData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.HighModuleData.grid(column=6, row=2)

		self.LowModule = ttk.Label(self.BatteryFrame, text='Low Module:')
		self.LowModule.grid(column=5, row=3)
		self.LowModuleData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.LowModuleData.grid(column=6, row=3)

		self.Balance = ttk.Label(self.BatteryFrame, text='Balance:')
		self.Balance.grid(column=3, row=4)
		self.BalanceData = ttk.Label(self.BatteryFrame, text='N/A', width=7)
		self.BalanceData.grid(column=4, row=4)

		self.names = [['bms_data'],
						['bms_node_data0'],
						['bms_node_data1'],
						['bms_node_data2'],
						['bms_node_data3'],
						['bms_node_data4'],
						['bms_node_data5'],
						['bms_node_data6'],
						['bms_node_data7']]

		self.records = []
		self.harnesses = []
		self.updaters = [self.updateBMS,
							self.updateNode0,
							self.updateNode1,
							self.updateNode2,
							self.updateNode3,
							self.updateNode4,
							self.updateNode5,
							self.updateNode6,
							self.updateNode7,
							self.updateNode8]
		self.modVolts = np.zeros(35)

	def HandleNewRecord(self, record):
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
			for i in range(len(self.records)):
				self.records[i].value_callback.append(self.updaters[i])
				self.records[i].Subscribe()

	def updateBMS(self, record):
		self.harnesses[0].buf = buffer(record.value)
		self.VoltageData['text'] = "%.2f" % self.harnesses[0]['sum_volts'].value
		if self.harnesses[0]['max_volts'].value > GLOBALS.BATT_OVERVOLT_THRESHOLD:
			self.CellVMaxData['foreground'] = 'red'
		else:
			self.CellVMaxData['foreground'] = 'black'
		if self.harnesses[0]['min_volts'].value < GLOBALS.BATT_UNDERVOLT_THRESHOLD:
			self.CellVMinData['foreground'] = 'red'
		else:
			self.CellVMinData['foreground'] = 'black'
		self.CurrentBMSData['text'] = "%.2f" % self.harnesses[0]['current'].value
		self.PowerInData['text'] = "%.2f" % (self.harnesses[0]['sum_volts'].value*self.harnesses[0]['current'].value)
		self.CellVMinData['text'] = "%.2f" % self.harnesses[0]['min_volts'].value
		self.CellVMaxData['text'] = "%.2f" % self.harnesses[0]['max_volts'].value
		self.TempData['text'] = "%.2f" % self.harnesses[0]['mean_temp'].value
		self.BalanceData['text'] = "%.2f" % (self.harnesses[0]['max_volts'].value - self.harnesses[0]['min_volts'].value)

	def updateNode0(self, record):
		self.harnesses[1].buf = buffer(record.value)
		self.modVolts[0] = self.harnesses[1]['node0_volts'].value
		self.modVolts[1] = self.harnesses[1]['node1_volts'].value 
		self.modVolts[2] = self.harnesses[1]['node2_volts'].value 
		self.modVolts[3] = self.harnesses[1]['node3_volts'].value 
		self.findHighLowModule()

	def updateNode1(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.modVolts[4] = self.harnesses[2]['node4_volts'].value
		self.modVolts[5] = self.harnesses[2]['node5_volts'].value 
		self.modVolts[6] = self.harnesses[2]['node6_volts'].value 
		self.modVolts[7] = self.harnesses[2]['node7_volts'].value 
		self.findHighLowModule()

	def updateNode2(self, record):
		self.harnesses[3].buf = buffer(record.value)
		self.modVolts[8] = self.harnesses[3]['node8_volts'].value 
		self.modVolts[9] = self.harnesses[3]['node9_volts'].value 
		self.modVolts[10] = self.harnesses[3]['node10_volts'].value 
		self.modVolts[11] = self.harnesses[3]['node11_volts'].value 
		self.findHighLowModule()

	def updateNode3(self, record):
		self.harnesses[4].buf = buffer(record.value)
		self.modVolts[12] = self.harnesses[4]['node12_volts'].value
		self.modVolts[13] = self.harnesses[4]['node13_volts'].value 
		self.modVolts[14] = self.harnesses[4]['node14_volts'].value 
		self.modVolts[15] = self.harnesses[4]['node15_volts'].value 
		self.findHighLowModule()

	def updateNode4(self, record):
		self.harnesses[5].buf = buffer(record.value)
		self.modVolts[16] = self.harnesses[5]['node16_volts'].value 
		self.modVolts[17] = self.harnesses[5]['node17_volts'].value 
		self.modVolts[18] = self.harnesses[5]['node18_volts'].value 
		self.modVolts[19] = self.harnesses[5]['node19_volts'].value 
		self.findHighLowModule()

	def updateNode5(self, record):
		self.harnesses[6].buf = buffer(record.value)
		self.modVolts[20] = self.harnesses[6]['node20_volts'].value 
		self.modVolts[21] = self.harnesses[6]['node21_volts'].value 
		self.modVolts[22] = self.harnesses[6]['node22_volts'].value 
		self.modVolts[23] = self.harnesses[6]['node23_volts'].value 
		self.findHighLowModule()

	def updateNode6(self, record):
		self.harnesses[7].buf = buffer(record.value)
		self.modVolts[24] = self.harnesses[7]['node24_volts'].value
		self.modVolts[25] = self.harnesses[7]['node25_volts'].value 
		self.modVolts[26] = self.harnesses[7]['node26_volts'].value 
		self.modVolts[27] = self.harnesses[7]['node27_volts'].value 
		self.findHighLowModule()

	def updateNode7(self, record):
		self.harnesses[8].buf = buffer(record.value)
		self.modVolts[28] = self.harnesses[8]['node28_volts'].value 
		self.modVolts[29] = self.harnesses[8]['node29_volts'].value 
		self.modVolts[30] = self.harnesses[8]['node30_volts'].value 
		self.modVolts[31] = self.harnesses[8]['node31_volts'].value 
		self.findHighLowModule()

	def updateNode8(self, record):
		self.harnesses[9].buf = buffer(record.value)
		self.modVolts[32] = self.harnesses[9]['node32_volts'].value 
		self.modVolts[33] = self.harnesses[9]['node33_volts'].value 
		self.modVolts[34] = self.harnesses[9]['node34_volts'].value 
		self.findHighLowModule()

	def findHighLowModule(self):
		self.HighModuleData['text'] = np.argmax(self.modVolts)
		self.LowModuleData['text'] = np.argmin(self.modVolts)
	