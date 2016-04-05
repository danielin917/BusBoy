import json
import Tkinter as tk 
import ttk
import threading
import time
import numpy as np 

import data_bus
import wire
from modules import GLOBALS

class BatteryTab(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.batTab = ttk.Panedwindow(self.parent, orient=tk.HORIZONTAL)

		self.battVoltFrame = ttk.Panedwindow(self.batTab, orient=tk.VERTICAL, width=460)
		self.batTab.add(self.battVoltFrame)

		self.battTempFrame = ttk.Panedwindow(self.batTab, orient=tk.VERTICAL, width=460)
		self.batTab.add(self.battTempFrame)

		self.battVolts = []
		self.battVoltTexts = []
		self.battTemps = []
		self.battTempTexts = []

		self.battVoltInit()
		self.battTempInit()

		self.harnesses = []
		self.recordNum = 0

		self.names = [['bms_node_data0'],
						['bms_node_data1'],
						['bms_node_data2'],
						['bms_node_data3'],
						['bms_node_data4'],
						['bms_node_data5'],
						['bms_node_data6'],
						['bms_node_data7'],
						['bms_node_data8']]

		self.updaters = [self.node0Updater,
							self.node1Updater,
							self.node2Updater,
							self.node3Updater,
							self.node4Updater,
							self.node5Updater,
							self.node6Updater,
							self.node7Updater,
							self.node8Updater]

		self.T_min = 20
		#self.voltColorRange = GLOBALS.BATT_OVERVOLT_THRESHOLD - GLOBALS.BATT_UNDERVOLT_THRESHOLD
		self.tempColorRange = GLOBALS.BATT_OVERTEMP_THRESHOLD - 20

		self.updateVoltColorsThread = threading.Thread(target=self.updateVoltColors)
		self.updateVoltColorsThread.Daemon = True
		self.updateVoltColorsThread.start()

		self.updateTempColorsThread = threading.Thread(target=self.updateTempColors)
		self.updateTempColorsThread.Daemon = True
		self.updateTempColorsThread.start()

	def battVoltInit(self):
		for i in range(6):
			row = ttk.Panedwindow(self.battVoltFrame, orient=tk.HORIZONTAL, height=50)
			self.battVoltFrame.add(row)
			for j in range(6):
				self.battVolts += [0]
				newFrame = ttk.Labelframe(row, text=str(6*i+j), width=90)
				row.add(newFrame)
				voltText = ttk.Label(newFrame, text='N/A', width=4, font=('helvetica', 14))
				voltText.grid(row=1, column=1)
				voltUnits = ttk.Label(newFrame, text='V', width=3)
				voltUnits.grid(row=1, column=2)
				self.battVoltTexts += [voltText]

	def battTempInit(self):
		for i in range(6):
			row = ttk.Panedwindow(self.battTempFrame, orient=tk.HORIZONTAL, height=50)
			self.battTempFrame.add(row)
			for j in range(6):
				self.battTemps += [0]
				newFrame = ttk.Labelframe(row, text=str(6*i+j), width=90)
				row.add(newFrame)
				tempText = ttk.Label(newFrame, text='N/A', width=4, font=('helvetica', 14))
				tempText.grid(row=1, column=1)
				tempUnits = ttk.Label(newFrame, text='C', width=3)
				tempUnits.grid(row=1, column=2)
				self.battTempTexts += [tempText]

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			record.value_callback.append(self.updaters[self.recordNum])
			record.Subscribe()
			self.recordNum += 1

	def node0Updater(self, record):
		self.harnesses[0].buf = buffer(record.value)
		for i in range(0,4):
			self.battVolts[i] = self.harnesses[0]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[0]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node1Updater(self, record):
		self.harnesses[1].buf = buffer(record.value)
		for i in range(4,8):
			self.battVolts[i] = self.harnesses[1]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[1]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node2Updater(self, record):
		self.harnesses[2].buf = buffer(record.value)
		for i in range(8,12):
			self.battVolts[i] = self.harnesses[2]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[2]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node3Updater(self, record):
		self.harnesses[3].buf = buffer(record.value)
		for i in range(12,16):
			self.battVolts[i] = self.harnesses[3]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[3]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node4Updater(self, record):
		self.harnesses[4].buf = buffer(record.value)
		for i in range(16,20):
			self.battVolts[i] = self.harnesses[4]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[4]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node5Updater(self, record):
		self.harnesses[5].buf = buffer(record.value)
		for i in range(20,24):
			self.battVolts[i] = self.harnesses[5]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[5]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node6Updater(self, record):
		self.harnesses[6].buf = buffer(record.value)
		for i in range(24,28):
			self.battVolts[i] = self.harnesses[6]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[6]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node7Updater(self, record):
		self.harnesses[7].buf = buffer(record.value)
		for i in range(28,32):
			self.battVolts[i] = self.harnesses[7]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[7]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def node8Updater(self, record):
		self.harnesses[8].buf = buffer(record.value)
		for i in range(32,35):
			self.battVolts[i] = self.harnesses[8]['node' + str(i) + '_volts'].value 
			self.battVoltTexts[i]['text'] = '%.2f' % self.battVolts[i]
			self.battTemps[i] = self.harnesses[8]['node' + str(i) + '_temp'].value 
			self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]

	def updateVoltColors(self):
		while True:
			voltRange = []
			Ids = []
			for i in range(len(self.battVolts)):
				if self.battVolts[i] < GLOBALS.BATT_UNDERVOLT_THRESHOLD:
					self.battVoltTexts[i]['foreground'] = 'black'
				elif self.battVolts[i] > GLOBALS.BATT_OVERVOLT_THRESHOLD:
					self.battVoltTexts[i]['foreground'] = 'black'
				else:
					voltRange += [self.battVolts[i]]
					Ids += [i]
			if len(voltRange) > 0:
				mean = np.mean(voltRange)
				stdDev = np.std(voltRange)
				j = 0
				while j < len(voltRange):
					if abs(voltRange[j] - mean) > 1.5*stdDev:
						self.battVoltTexts[Ids[j]]['foreground'] = 'black'
						del voltRange[j]
						del Ids[j]
					else:
						j += 1
				voltColorRange = max(voltRange) - min(voltRange)
				if voltColorRange > 0:
					for k in range(len(voltRange)):
						V_rel = voltRange[k] - min(voltRange)
						norm = V_rel/voltColorRange
						redHexVal = int(norm*4095)
						blueHexVal = 4095 - redHexVal
						redHex = hex(redHexVal)
						blueHex = hex(blueHexVal)
						for m in range(5 - len(redHex)):
							redHex = redHex[:2] + '0' + redHex[2:]
						for n in range(5 - len(blueHex)):
							blueHex = blueHex[:2] + '0' + blueHex[2:]
						self.battVoltTexts[Ids[k]]['foreground'] = '#' + redHex[2:] + '000' + blueHex[2:]

	def updateTempColors(self):
		while True:
			tempRange = []
			Ids = []
			for i in range(len(self.battTemps)):
				if self.battTemps[i] < self.T_min:
					self.battTempTexts[i]['foreground'] = 'black'
				elif self.battTemps[i] > GLOBALS.BATT_OVERTEMP_THRESHOLD:
					self.battTempTexts[i]['foreground'] = 'black'
				else:
					tempRange += [self.battTemps[i]]
					Ids += [i]
			if len(tempRange) > 0:
				mean = np.mean(tempRange)
				stdDev = np.std(tempRange)
				j = 0
				while j < len(tempRange):
					if abs(tempRange[j] - mean) > 1.5*stdDev:
						self.battTempTexts[Ids[j]]['foreground'] = 'black'
						del tempRange[j]
						del Ids[j]
					else:
						j += 1
				tempColorRange = max(tempRange) - min(tempRange)
				if tempColorRange > 0:
					for k in range(len(tempRange)):
						T_rel = tempRange[k] - min(tempRange)
						norm = T_rel/tempColorRange
						redHexVal = int(norm*4095)
						blueHexVal = 4095 - redHexVal
						redHex = hex(redHexVal)
						blueHex = hex(blueHexVal)
						for m in range(5 - len(redHex)):
							redHex = redHex[:2] + '0' + redHex[2:]
						for n in range(5 - len(blueHex)):
							blueHex = blueHex[:2] + '0' + blueHex[2:]
						self.battTempTexts[Ids[k]]['foreground'] = '#' + redHex[2:] + '000' + blueHex[2:]
