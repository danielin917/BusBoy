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

class Reliability_Breaker(ttk.Frame):
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

		self.breakerFrame = ttk.Labelframe(self.parent, text='Breaker')
		self.breakerFrame.propagate(0)

		self.breakerFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.breakerFig, master=self.breakerFrame)
		self.canvas.get_tk_widget().pack()
		self.breakerPlot = self.breakerFig.add_subplot(111)
		self.breakerLine, = self.breakerPlot.plot(self.t, self.messages, 'g-')
		self.breakerPlot.set_xlim([0, 300])
		self.breakerPlot.set_ylim([0,100])
		self.breakerPlot.invert_xaxis()
		self.breakerFig.subplots_adjust(bottom=.15, right=.95)

		self.data_bus.record_callback.append(self.handleNewRecord)
		self.updaters.append(self.updateGraph)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name == 'cur0':
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
				if not self.Alarms.alarmPlaying[6]:
					self.Alarms.breakerLabel['foreground'] = 'red'
					self.Alarms.alarmPlaying[6] = True
					breakerAlarm = 'overvolt_alarm.wav'
					breakerAlarmThread = threading.Thread(target=self.Alarms.playAlarm, args=[breakerAlarm, 6])
					breakerAlarmThread.Daemon = True
					breakerAlarmThread.start()
		else:
			self.Alarms.breakerLabel['foreground'] = 'black'
			self.timing = False
			self.Alarms.alarmPlaying[6] = False
		self.numMessages = 0
		if self.t[1] > 300:
			del self.t[0]
			del self.messages[0]
		self.breakerLine.set_xdata(self.t)
		self.breakerLine.set_ydata(self.messages)
		self.breakerFig.canvas.draw()
		