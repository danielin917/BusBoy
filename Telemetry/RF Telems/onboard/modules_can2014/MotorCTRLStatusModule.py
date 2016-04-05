import datetime
import time
import json
import Tkinter as tk 
import ttk

import data_bus
import wire

class MotorCTRLStatusModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.MotorCTRLStatusFrame = ttk.Labelframe(self.parent, text='Motor CTRL Status', width=200)

		self.limitLabel = ttk.Label(self.MotorCTRLStatusFrame, text='N/A', width=20)
		self.limitLabel.grid(column=1, row=1)

		self.errorLabel = ttk.Label(self.MotorCTRLStatusFrame, text='N/A', width=20)
		self.errorLabel.grid(column=1, row=2)

		self.motLabel = ttk.Label(self.MotorCTRLStatusFrame, text='N/A', width=20)
		self.motLabel.grid(column=1, row=3)

		self.txRxErrLabel = ttk.Label(self.MotorCTRLStatusFrame, text='N/A', width=20)
		self.txRxErrLabel.grid(column=1, row=4)

		self.limit2Label = ttk.Label(self.MotorCTRLStatusFrame, text='N/A')
		self.limit2Label.grid(column=2, row=1)

		self.error2Label = ttk.Label(self.MotorCTRLStatusFrame, text='N/A')
		self.error2Label.grid(column=2, row=2)

		self.mot2Label = ttk.Label(self.MotorCTRLStatusFrame, text='N/A')
		self.mot2Label.grid(column=2, row=3)

		self.txRxErr2Label = ttk.Label(self.MotorCTRLStatusFrame, text='N/A')
		self.txRxErr2Label.grid(column=2, row=4)

		self.harnesses = []

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name == ['motflag']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			record.value_callback.append(self.handleValue)
			record.Subscribe()
		elif name == ['motflag2']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			record.value_callback.append(self.handleValue2)
			record.Subscribe()

	def handleValue(self, record):
		self.harnesses[0].buf = buffer(record.value)
		lim_d = self.harnesses[0]['lim'].value
		limText = 'lim: (' + str(lim_d) + ') '
		if ((lim_d & 0b00000001) != 0):
			limText += 'Temp'
		if ((lim_d & 0b00000010) != 0):
			limText += 'Bus Volt Low'
		if ((lim_d & 0b00000100) != 0):
			limText += 'Bus Volt High'
		if ((lim_d & 0b00001000) != 0):
			limText += 'Bus Current'
		if ((lim_d & 0b00010000) != 0):
			limText += 'Velocity'
		if ((lim_d & 0b00100000) != 0):
			limText += 'Motor Curr'
		if ((lim_d & 0b01000000) != 0):
			limText += 'Out Volt PWM'
		self.limitLabel['text'] = limText

		error = self.harnesses[0]['err'].value 
		if error == 0:
			self.errorLabel['text'] = 'err: OK (' + str(error) + ')'
			self.errorLabel['foreground'] = 'black'
		else:
			errorText = 'err: (' + str(error) + ') '
			if ((error & 0b00000001) != 0):
				errorText += 'Dsat, '
			if ((error & 0b00000010) != 0):
				errorText += '15Vrail, '
			if ((error & 0b00000100) != 0):
				errorText += 'CfgReadErr, '
			if ((error & 0b00001000) != 0):
				errorText += 'WatchdogRst, '
			if ((error & 0b00010000) != 0):
				errorText += 'Hall, '
			if ((error & 0b00100000) != 0):
				errorText += 'DC Overvolt, '
			if ((error & 0b01000000) != 0):
				errorText += 'SW OverCurr, '
			if ((error & 0b10000000) != 0):
				errorText += 'HW OverCurr'
			self.errorLabel['text'] = errorText
			self.errorLabel['foreground'] = 'red'

		self.motLabel['text'] = 'mot: ' + str(self.harnesses[0]['mot'].value)

		self.txRxErrLabel['text'] = 'txerr: ' + str(self.harnesses[0]['txerr'].value) + ' rxerr: ' + str(self.harnesses[0]['rxerr'].value)

	def handleValue2(self, record):
		self.harnesses[1].buf = buffer(record.value)
		lim_d = self.harnesses[1]['lim'].value
		limText = 'lim: (' + str(lim_d) + ') '
		if ((lim_d & 0b00000001) != 0):
			limText += 'Temp'
		if ((lim_d & 0b00000010) != 0):
			limText += 'Bus Volt Low'
		if ((lim_d & 0b00000100) != 0):
			limText += 'Bus Volt High'
		if ((lim_d & 0b00001000) != 0):
			limText += 'Bus Current'
		if ((lim_d & 0b00010000) != 0):
			limText += 'Velocity'
		if ((lim_d & 0b00100000) != 0):
			limText += 'Motor Curr'
		if ((lim_d & 0b01000000) != 0):
			limText += 'Out Volt PWM'
		self.limit2Label['text'] = limText

		error = self.harnesses[1]['err'].value 
		if error == 0:
			self.error2Label['text'] = 'err: OK (' + str(error) + ')'
			self.error2Label['foreground'] = 'black'
		else:
			errorText = 'err: (' + str(error) + ') '
			if ((error & 0b00000001) != 0):
				errorText += 'Dsat, '
			if ((error & 0b00000010) != 0):
				errorText += '15Vrail, '
			if ((error & 0b00000100) != 0):
				errorText += 'CfgReadErr, '
			if ((error & 0b00001000) != 0):
				errorText += 'WatchdogRst, '
			if ((error & 0b00010000) != 0):
				errorText += 'Hall, '
			if ((error & 0b00100000) != 0):
				errorText += 'DC Overvolt, '
			if ((error & 0b01000000) != 0):
				errorText += 'SW OverCurr, '
			if ((error & 0b10000000) != 0):
				errorText += 'HW OverCurr'
			self.error2Label['text'] = errorText
			self.error2Label['foreground'] = 'red'

		self.mot2Label['text'] = 'mot: ' + str(self.harnesses[1]['mot'].value)

		self.txRxErr2Label['text'] = 'txerr: ' + str(self.harnesses[1]['txerr'].value) + ' rxerr: ' + str(self.harnesses[1]['rxerr'].value)
