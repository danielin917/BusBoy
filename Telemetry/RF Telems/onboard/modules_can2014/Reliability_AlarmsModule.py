import sys
import json
import Tkinter as tk
import ttk
import pyglet
import os
import threading
import time

import data_bus
import wire
import GLOBALS

class Reliability_Alarms(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.alarmsFrame = ttk.Labelframe(self.parent, text='Alarms')

		self.battOvervoltAlarmOn = tk.IntVar()
		self.battOvervoltAlarmOn.set(1)
		self.battOvervoltButton = ttk.Checkbutton(self.alarmsFrame, variable=self.battOvervoltAlarmOn)
		self.battOvervoltButton.grid(row=1, column=1)
		self.battOvervoltLabel = ttk.Label(self.alarmsFrame, text='batt Overvolt')
		self.battOvervoltLabel.grid(row=1, column=2)

		self.battUndervoltAlarmOn = tk.IntVar()
		self.battUndervoltAlarmOn.set(1)
		self.battUndervoltButton = ttk.Checkbutton(self.alarmsFrame, variable=self.battUndervoltAlarmOn)
		self.battUndervoltButton.grid(row=2, column=1)
		self.battUndervoltLabel = ttk.Label(self.alarmsFrame, text='batt Undervolt')
		self.battUndervoltLabel.grid(row=2, column=2)

		self.battOverTempAlarmOn = tk.IntVar()
		self.battOverTempAlarmOn.set(1)
		self.battOverTempButton = ttk.Checkbutton(self.alarmsFrame, variable=self.battOverTempAlarmOn)
		self.battOverTempButton.grid(row=3, column=1)
		self.battOvertempLabel = ttk.Label(self.alarmsFrame, text='batt Overtemp')
		self.battOvertempLabel.grid(row=3, column=2)

		self.WSOverTempAlarmOn = tk.IntVar()
		self.WSOverTempAlarmOn.set(1)
		self.WSOverTempButton = ttk.Checkbutton(self.alarmsFrame, variable=self.WSOverTempAlarmOn)
		self.WSOverTempButton.grid(row=4, column=1)
		self.WSOvertempLabel = ttk.Label(self.alarmsFrame, text='WS Overtemp')
		self.WSOvertempLabel.grid(row=4, column=2)

		self.motCtrlErrorAlarmOn = tk.IntVar()
		self.motCtrlErrorAlarmOn.set(1)
		self.motCtrlErrorButton = ttk.Checkbutton(self.alarmsFrame, variable=self.motCtrlErrorAlarmOn)
		self.motCtrlErrorButton.grid(row=5, column=1)
		self.motCtrlErrorLabel = ttk.Label(self.alarmsFrame, text='mot Ctrl Error')
		self.motCtrlErrorLabel.grid(row=5, column=2)

		self.BMSMasterAlarmOn = tk.IntVar()
		self.BMSMasterAlarmOn.set(0)
		self.BMSMasterButton = ttk.Checkbutton(self.alarmsFrame, variable=self.BMSMasterAlarmOn)
		self.BMSMasterButton.grid(row=1, column=3)
		self.BMSMasterLabel = ttk.Label(self.alarmsFrame, text='BMS Master')
		self.BMSMasterLabel.grid(row=1, column=4)

		self.breakerAlarmOn = tk.IntVar()
		self.breakerAlarmOn.set(0)
		self.breakerButton = ttk.Checkbutton(self.alarmsFrame, variable=self.breakerAlarmOn)
		self.breakerButton.grid(row=2, column=3)
		self.breakerLabel = ttk.Label(self.alarmsFrame, text='Breaker')
		self.breakerLabel.grid(row=2, column=4)

		self.steeringAlarmOn = tk.IntVar()
		self.steeringAlarmOn.set(0)
		self.steeringButton = ttk.Checkbutton(self.alarmsFrame, variable=self.steeringAlarmOn)
		self.steeringButton.grid(row=3, column=3)
		self.steeringLabel = ttk.Label(self.alarmsFrame, text='Steering')
		self.steeringLabel.grid(row=3, column=4)

		self.waveSculptorAlarmOn = tk.IntVar()
		self.waveSculptorAlarmOn.set(0)
		self.waveSculptorButton = ttk.Checkbutton(self.alarmsFrame, variable=self.waveSculptorAlarmOn)
		self.waveSculptorButton.grid(row=4, column=3)
		self.waveSculptorLabel = ttk.Label(self.alarmsFrame, text='Wavesculptor')
		self.waveSculptorLabel.grid(row=4, column=4)

		self.alarmStatus = [self.battOvervoltAlarmOn,
								self.battUndervoltAlarmOn,
								self.battOverTempAlarmOn,
								self.motCtrlErrorAlarmOn,
								self.WSOverTempAlarmOn,
								self.BMSMasterAlarmOn,
								self.breakerAlarmOn,
								self.steeringAlarmOn,
								self.waveSculptorAlarmOn]

		self.alarmPlaying = [0, 0, 0, 0, 0, 0, 0, 0, 0]

		self.records = []
		self.harnesses = []

		self.names = [['bms_data'],
						['motflag'],
						['mottemp0'],
						['motflag2'],
						['mottemp0_2']]

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
		if name == ['bms_data']:
			record.value_callback.append(self.handleBattValue)
			record.Subscribe()
		elif name == ['motflag']:
			record.value_callback.append(self.handleMotCtrlValue)
			record.Subscribe()
		elif name == ['mottemp0']:
			record.value_callback.append(self.handleWSValue)
			record.Subscribe()
		elif name == ['motflag2']:
			record.value_callback.append(self.handleMot2CtrlValue)
			record.Subscribe()
		elif name == ['mottemp0_2']:
			record.value_callback.append(self.handleWS2Value)
			record.Subscribe()

	def handleBattValue(self, record):
		self.harnesses[0].buf = buffer(record.value)
		if self.harnesses[0]['max_volts'].value > GLOBALS.BATT_OVERVOLT_THRESHOLD:
			self.battOvervoltLabel['foreground'] = 'red'
			if self.alarmStatus[0].get():
				if not self.alarmPlaying[0]:
					self.alarmPlaying[0] = 1
					overvoltAlarm = 'overvolt_alarm.wav'
					overvoltAlarmThread = threading.Thread(target=self.playAlarm, args=[overvoltAlarm, 0])
					overvoltAlarmThread.Daemon = True
					overvoltAlarmThread.start()
		else:
			self.alarmPlaying[0] = 0 
			self.battOvervoltLabel['foreground'] = 'black'

		if self.harnesses[0]['min_volts'].value < GLOBALS.BATT_UNDERVOLT_THRESHOLD:
			self.battUndervoltLabel['foreground'] = 'red'
			if self.alarmStatus[1].get():
				if not self.alarmPlaying[1]:
					self.alarmPlaying[1] = 1
					undervoltAlarm = 'undervolt_alarm.wav'
					undervoltAlarmThread = threading.Thread(target=self.playAlarm, args=[undervoltAlarm, 1])
					undervoltAlarmThread.Daemon = True
					undervoltAlarmThread.start()
		else:
			self.alarmPlaying[1] = 0
			self.battUndervoltLabel['foreground'] = 'black'

		if self.harnesses[0]['max_temp'].value > GLOBALS.BATT_OVERTEMP_THRESHOLD:
			self.battOvertempLabel['foreground'] = 'red'
			if self.alarmStatus[2].get():
				if not self.alarmPlaying[2]:
					self.alarmPlaying[2] = 1
					overtempAlarm = 'batt_overtemp_alarm.wav'
					overtempAlarmThread = threading.Thread(target=self.playAlarm, args=[overtempAlarm, 2])
					overtempAlarmThread.Daemon = True
					overtempAlarmThread.start()
		else:
			self.alarmPlaying[2] = 0
			self.battOvertempLabel['foreground'] = 'black'

	def handleMotCtrlValue(self, record):
		self.harnesses[1].buf = buffer(record.value)
		if self.harnesses[1]['err'].value != 0:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		elif self.harnesses[1]['rxerr'].value > 128:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		elif self.harnesses[1]['txerr'].value > 128:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		else:
			self.alarmPlaying[3] = 0
			self.motCtrlErrorLabel['foreground'] = 'black'

	def handleWSValue(self, record):
		self.harnesses[2].buf = buffer(record.value)
		if self.harnesses[2]['motor'].value > GLOBALS.WS_OVERTEMP_THRESHOLD:
			self.WSOvertempLabel['foreground'] = 'red'
			if self.alarmStatus[4].get():
				if not self.alarmPlaying[4]:
					self.alarmPlaying[4] = 1
					wsAlarm = 'hs_overtemp_alarm.wav'
					wsAlarmThread = threading.Thread(target=self.playAlarm, args=[wsAlarm, 4])
					wsAlarmThread.Daemon = True
					wsAlarmThread.start()
		else:
			self.alarmPlaying[4] = 0
			self.WSOvertempLabel['foreground'] = 'black'

	def handleMot2CtrlValue(self, record):
		self.harnesses[3].buf = buffer(record.value)
		if self.harnesses[3]['err'].value != 0:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		elif self.harnesses[3]['rxerr'].value > 128:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		elif self.harnesses[3]['txerr'].value > 128:
			self.motCtrlErrorLabel['foreground'] = 'red'
			if self.alarmStatus[3].get():
				if not self.alarmPlaying[3]:
					self.alarmPlaying[3] = 1
					motCtrlAlarm = 'mot_ctrl_err_alarm.wav'
					motCtrlAlarmThread = threading.Thread(target=self.playAlarm, args=[motCtrlAlarm, 3])
					motCtrlAlarmThread.Daemon = True
					motCtrlAlarmThread.start()
		else:
			self.alarmPlaying[3] = 0
			self.motCtrlErrorLabel['foreground'] = 'black'

	def handleWS2Value(self, record):
		self.harnesses[4].buf = buffer(record.value)
		if self.harnesses[4]['motor'].value > GLOBALS.WS_OVERTEMP_THRESHOLD:
			self.WSOvertempLabel['foreground'] = 'red'
			if self.alarmStatus[4].get():
				if not self.alarmPlaying[4]:
					self.alarmPlaying[4] = 1
					wsAlarm = 'hs_overtemp_alarm.wav'
					wsAlarmThread = threading.Thread(target=self.playAlarm, args=[wsAlarm, 4])
					wsAlarmThread.Daemon = True
					wsAlarmThread.start()
		else:
			self.alarmPlaying[4] = 0
			self.WSOvertempLabel['foreground'] = 'black'

	def playAlarm(self, alarmName, alarmNum):
		while (self.alarmStatus[alarmNum].get() and self.alarmPlaying[alarmNum]):
			alarm = pyglet.media.load(os.path.join(sys.path[0], 'resources', alarmName))
			alarm.play()
			time.sleep(.15)
		self.alarmPlaying[alarmNum] = 0