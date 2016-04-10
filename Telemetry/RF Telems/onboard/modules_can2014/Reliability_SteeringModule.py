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

class SteeringUpdater(object):
	def __init__(self, records, steeringFig, steeringPlot, Alarms):
		self.records = records
		self.steeringFig = steeringFig
		self.steeringPlot = steeringPlot
		self.Alarms = Alarms

		self.numMessages = 0
		self.t1 = time.clock()
		self.messages = [0]
		self.t = [0]

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
					if not self.Alarms.alarmPlaying[7]:
						self.Alarms.steeringLabel['foreground'] = 'red'
						self.Alarms.alarmPlaying[7] = True
						steeringAlarm = 'overvolt_alarm.wav'
						steeringAlarmThread = threading.Thread(target=self.Alarms.playAlarm, args=[steeringAlarm, 7])
						steeringAlarmThread.Daemon = True
						steeringAlarmThread.start()
			else:
				self.Alarms.steeringLabel['foreground'] = 'black'
				self.timing = False
				self.Alarms.alarmPlaying[7] = False
			self.numMessages = 0
			if len(self.t) > 80:
				del self.t[0]
				del self.messages[0]
			self.steeringPlot.clear()
			self.steeringPlot.plot(self.t, self.messages, 'b-')
			self.steeringPlot.set_xlim([0, self.t[0]])
			self.steeringPlot.invert_xaxis()
			self.steeringFig.canvas.draw()

class Reliability_Steering(ttk.Frame):
	def __init__(self, root, parent, data_bus, Alarms):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.Alarms = Alarms
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.steeringFrame = ttk.Labelframe(self.parent, text='Steering')
		self.steeringFrame.propagate(0)

		self.steeringFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.steeringFig, master=self.steeringFrame)
		self.canvas.get_tk_widget().pack()
		self.steeringPlot = self.steeringFig.add_subplot(111)
		self.steeringFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []

		self.names = [['motcmd'],
						['motbuscmd'],
						['lighting'],
						['cruise'],
						['cruiseconstants'],
						['cruisestate'],
						['driverresponse']]

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			self.records += [record]
		elif name ==['motcmd2']:   
			self.records += [record]
			self.updaterThread = threading.Thread(target=SteeringUpdater, args=(self.records, self.steeringFig, self.steeringPlot, self.Alarms))
			self.updaterThread.Daemon = True
			self.updaterThread.start()