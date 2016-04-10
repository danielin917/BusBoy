import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class Charge_AuxPackModule(ttk.Frame):
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

		self.auxPackFrame = ttk.Labelframe(self.parent, text='Aux Pack')
		self.auxPackFrame.propagate(0)

		self.aux0 = ttk.Label(self.auxPackFrame, text='Aux 0', font=('Helvetica', 16))
		self.aux0.grid(row=1, column=1)

		self.aux0Volt = ttk.Label(self.auxPackFrame, text='Voltage:')
		self.aux0Volt.grid(row=2, column=1)
		self.aux0VoltData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0VoltData.grid(row=2, column=2)
		self.aux0Temp = ttk.Label(self.auxPackFrame, text='Temp:')
		self.aux0Temp.grid(row=2, column=3)
		self.aux0TempData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0TempData.grid(row=2, column=4)

		self.aux0Vmax = ttk.Label(self.auxPackFrame, text='V max:')
		self.aux0Vmax.grid(row=3, column=1)
		self.aux0VmaxData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0VmaxData.grid(row=3, column=2)
		self.aux0HighModule = ttk.Label(self.auxPackFrame, text='High Module:')
		self.aux0HighModule.grid(row=3, column=3)
		self.aux0HighModuleData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0HighModuleData.grid(row=3, column=4)

		self.aux0Vmin = ttk.Label(self.auxPackFrame, text='V min:')
		self.aux0Vmin.grid(row=4, column=1)
		self.aux0VminData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0VminData.grid(row=4, column=2)
		self.aux0LowModule = ttk.Label(self.auxPackFrame, text='Low Module:')
		self.aux0LowModule.grid(row=4, column=3)
		self.aux0LowModuleData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0LowModuleData.grid(row=4, column=4)

		self.aux0Error = ttk.Label(self.auxPackFrame, text='Error:')
		self.aux0Error.grid(row=5, column=1)
		self.aux0ErrorData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux0ErrorData.grid(row=5, column=2)

		spacer = ttk.Label(self.auxPackFrame, text='    ')
		spacer.grid(row=6, column=1)

		self.aux1 = ttk.Label(self.auxPackFrame, text='Aux 1', font=('Helvetica', 16))
		self.aux1.grid(row=7, column=1)
		
		self.aux1Volt = ttk.Label(self.auxPackFrame, text='Voltage:')
		self.aux1Volt.grid(row=8, column=1)
		self.aux1VoltData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1VoltData.grid(row=8, column=2)
		self.aux1Temp = ttk.Label(self.auxPackFrame, text='Temp:')
		self.aux1Temp.grid(row=8, column=3)
		self.aux1TempData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1TempData.grid(row=8, column=4)

		self.aux1Vmax = ttk.Label(self.auxPackFrame, text='V max:')
		self.aux1Vmax.grid(row=9, column=1)
		self.aux1VmaxData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1VmaxData.grid(row=9, column=2)
		self.aux1HighModule = ttk.Label(self.auxPackFrame, text='High Module:')
		self.aux1HighModule.grid(row=9, column=3)
		self.aux1HighModuleData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1HighModuleData.grid(row=9, column=4)
		
		self.aux1Vmin = ttk.Label(self.auxPackFrame, text='V min:')
		self.aux1Vmin.grid(row=10, column=1)
		self.aux1VminData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1VminData.grid(row=10, column=2)
		self.aux1LowModule = ttk.Label(self.auxPackFrame, text='Low Module:')
		self.aux1LowModule.grid(row=10, column=3)
		self.aux1LowModuleData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1LowModuleData.grid(row=10, column=4)
		
		self.aux1Error = ttk.Label(self.auxPackFrame, text='Error:')
		self.aux1Error.grid(row=11, column=1)
		self.aux1ErrorData = ttk.Label(self.auxPackFrame, text='N/A', width=5)
		self.aux1ErrorData.grid(row=11, column=2)

		self.updatersDict = {'bmsvoltextremes_aux' : self.handleExtremes,
							'bms_pack_volts' : self.handlePackVolts}

		self.harnesses = {}

		self.data_bus.record_callback.append(self.handleNewRecord)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def handleExtremes(self, record):
		self.harnesses['bmsvoltextremes_aux'].buf = buffer(record.value)
		self.aux0VmaxData['text'] = '%.3f' % self.harnesses['bmsvoltextremes_aux']['max'].value
		self.aux0VminData['text'] = '%.3f' % self.harnesses['bmsvoltextremes_aux']['min'].value 
		self.aux0HighModuleData['text'] = self.harnesses['bmsvoltextremes_aux']['maxdex'].value 
		self.aux0LowModuleData['text'] = self.harnesses['bmsvoltextremes_aux']['mindex'].value 

	def handlePackVolts(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		self.aux0VoltData['text'] = '%.3f' % (self.harnesses['bms_pack_volts']['pack1'].value - self.harnesses['bms_pack_volts']['pack0'].value)