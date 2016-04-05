import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg 
import threading

import data_bus
import wire

class Reliability_BMSMaster(ttk.Frame):
	def __init__(self, parent, data_bus, Alarms, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.Alarms = Alarms
		self.updaters = updaters

		self.messages = [0, 0]
		self.t = [0, 0]
		self.numMessages = 0
		self.t1 = time.clock()
		self.timing = False

		self.BMSMasterFrame = ttk.Labelframe(self.parent, text='BMS Master')
		self.BMSMasterFrame.propagate(0)

		self.BMSFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.BMSFig, master=self.BMSMasterFrame)
		self.canvas.get_tk_widget().pack()
		self.BMSPlot = self.BMSFig.add_subplot(111)
		self.BMSLine, = self.BMSPlot.plot(self.t, self.messages, 'y-')
		self.BMSPlot.set_xlim([0, 300])
		self.BMSPlot.set_ylim([0, 100])
		self.BMSPlot.invert_xaxis()
		self.BMSFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []

		self.names = ['bms_volts_0',
						'bms_volts_1',
						'bms_volts_2',
						'bms_volts_3',
						'bms_volts_4',
						'bms_volts_5',
						'bms_volts_6',
						'bms_volts_7',
						'bms_volts_8',
						'bms_volts_9',
						'bms_volts_10',
						'bmsvoltextremes',
						'bms_pack_volts',
						'bms_temp_0',
						'bms_temp_1',
						'bms_temp_2',
						'bms_temp_3',
						'bms_temp_4',
						'bms_temp_5',
						'bms_temp_6',
						'bms_temp_7',
						'bms_temp_8',
						'bms_temp_9',
						'bms_temp_10',
						'bmstempextremes',
						'ab_current',
						'micro_current',
						'SOC']

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.names:
			record.value_callback.append(self.handleValue)
			record.Subscribe()

	def handleValue(self, record):
		self.numMessages += 1

	def updateGraph(self):
		dt = time.clock() - self.t1
		self.t1 += dt
		self.t = [t+dt for t in self.t]
		self.t += [0]
		self.messages += [self.numMessages/dt]
		if self.numMessages == 0:
			if not self.timing:
				self.timingBegin = time.clock()
				self.timing = True
			elif time.clock() - self.timingBegin > 30:
				if not self.Alarms.alarmPlaying[5]:
					self.Alarms.BMSMasterLabel['foreground'] = 'red'
					self.Alarms.alarmPlaying[5] = True
					BMSAlarm = 'overvolt_alarm.wav'
					BMSAlarmThread = threading.Thread(target=self.Alarms.playAlarm, args=[BMSAlarm, 5])
					BMSAlarmThread.Daemon = True
					BMSAlarmThread.start()
		else:
			self.Alarms.BMSMasterLabel['foreground'] = 'black'
			self.timing = False
			self.Alarms.alarmPlaying[5] = False
		self.numMessages = 0
		if self.t[1] > 300:
			del self.t[0]
			del self.messages[0]
		self.BMSLine.set_xdata(self.t)
		self.BMSLine.set_ydata(self.messages)
		self.BMSFig.canvas.draw()