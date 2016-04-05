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
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

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

		self.updatersDict = {'bmsvoltextremes' : self.updateVoltExtremes,
							'bms_pack_volts' : self.updatePackVolts,
							'bmstempextremes' : self.updateTemp,
							'ab_current' : self.updateCurrent}

		self.harnesses = {}
		self.V_tot = 0
		self.cur_tot = 0

		self.data_bus.record_callback.append(self.HandleNewRecord)

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def updateVoltExtremes(self, record):
		self.harnesses['bmsvoltextremes'].buf = buffer(record.value)
		self.CellVMinData['text'] = "%.2f" % self.harnesses['bmsvoltextremes']['min'].value
		if self.harnesses['bmsvoltextremes']['min'].value < GLOBALS.BATT_UNDERVOLT_THRESHOLD:
			self.CellVMinData['foreground'] = 'red'
		else:
			self.CellVMinData['foreground'] = 'black'
		self.LowModuleData['text'] = self.harnesses['bmsvoltextremes']['mindex']
		self.CellVMaxData['text'] = "%.2f" % self.harnesses['bmsvoltextremes']['max'].value
		if self.harnesses['bmsvoltextremes']['max'].value > GLOBALS.BATT_OVERVOLT_THRESHOLD:
			self.CellVMaxData['foreground'] = 'red'
		else:
			self.CellVMaxData['foreground'] = 'black'
		self.HighModuleData['text'] = self.harnesses['bmsvoltextremes']['maxdex']
		self.BalanceData['text'] = "%.2f" % self.harnesses['bmsvoltextremes']['balance'].value

	def updatePackVolts(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		self.V_tot = self.harnesses['bms_pack_volts']['pack0'].value
		self.VoltageData['text'] = "%.2f" % self.V_tot
		self.PowerInData['text'] = "%.2f" % (self.V_tot*self.cur_tot)

	def updateTemp(self, record):
		self.harnesses['bmstempextremes'].buf = buffer(record.value)
		self.TempData['text'] = "%.2f" % self.harnesses['bmstempextremes']['max'].value

	def updateCurrent(self, record):
		self.harnesses['ab_current'].buf = buffer(record.value)
		self.cur_tot = -1*self.harnesses['ab_current']['battery'].value
		self.CurrentBMSData['text'] = '%.2f' % self.cur_tot
		self.PowerInData['text'] = '%.2f' % (self.V_tot*self.cur_tot)
