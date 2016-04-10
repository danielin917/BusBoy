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

class Reliability_Steering(ttk.Frame):
	def __init__(self, parent, data_bus, Alarms, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.Alarms = Alarms
		self.updaters = updaters

		self.numMessages = 0
		self.t1 = time.clock()
		self.messages = [0, 0]
		self.t = [0, 0]
		self.timing = False

		self.steeringFrame = ttk.Labelframe(self.parent, text='Steering')
		self.steeringFrame.propagate(0)

		self.steeringFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.steeringFig, master=self.steeringFrame)
		self.canvas.get_tk_widget().pack()
		self.steeringPlot = self.steeringFig.add_subplot(111)
		self.steeringLine, = self.steeringPlot.plot(self.t, self.messages, 'b-')
		self.steeringPlot.set_xlim([0, 300])
		self.steeringPlot.set_ylim([0, 100])
		self.steeringPlot.invert_xaxis()
		self.steeringFig.subplots_adjust(bottom=.15, right=.95)

		self.names = ['motcmd',
						'motbuscmd',
						'lighting',
						'cruise',
						'motcmd2',
						'motbuscmd2']

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
		if self.t[1] > 300:
			del self.t[0]
			del self.messages[0]
		self.steeringLine.set_xdata(self.t)
		self.steeringLine.set_ydata(self.messages)
		self.steeringFig.canvas.draw()