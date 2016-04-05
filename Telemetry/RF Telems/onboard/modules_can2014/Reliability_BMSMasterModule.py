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

class BMSUpdater(object):
	def __init__(self, records, BMSFig, BMSPlot, Alarms):
		self.records = records
		self.BMSFig = BMSFig
		self.BMSPlot = BMSPlot
		self.Alarms = Alarms
		self.messages = [0]
		self.t = [0]
		self.numMessages = 0
		self.t1 = time.clock()

		for record in self.records:
			record.value_callback.append(self.handleValue)
			record.Subscribe()

		self.timing = False

		self.updateGraph()

	def handleValue(self, record):
		self.numMessages += 1

	def updateGraph(self):
		while True:
			dt = time.clock() - self.t1
			self.t1 += dt
			self.t = [t+dt for t in self.t]
			self.t += [0]
			self.messages += [self.numMessages]
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
			if len(self.t) > 80:
				del self.t[0]
				del self.messages[0]
			self.BMSPlot.clear()
			self.BMSPlot.plot(self.t, self.messages, 'y-')
			self.BMSPlot.set_xlim([0, self.t[0]])
			self.BMSPlot.invert_xaxis()
			self.BMSFig.canvas.draw()

class Reliability_BMSMaster(ttk.Frame):
	def __init__(self, root, parent, data_bus, Alarms):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.Alarms = Alarms
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.BMSMasterFrame = ttk.Labelframe(self.parent, text='BMS Master')
		self.BMSMasterFrame.propagate(0)

		self.BMSFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.BMSFig, master=self.BMSMasterFrame)
		self.canvas.get_tk_widget().pack()
		self.BMSPlot = self.BMSFig.add_subplot(111)
		self.BMSFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []

		self.names = [['bms_data'],
						['bms_status'],
						['bms_node_data0'],
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
			self.records += [record]
		elif name ==['bms_node_data8']:
			self.records += [record]
			self.updaterThread = threading.Thread(target=BMSUpdater, args=(self.records, self.BMSFig, self.BMSPlot, self.Alarms))
			self.updaterThread.daemon = True
			self.updaterThread.start()