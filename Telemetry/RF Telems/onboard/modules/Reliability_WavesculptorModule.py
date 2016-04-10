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

class Reliability_Wavesculptor(ttk.Frame):
	def __init__(self, parent, data_bus, Alarms, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.Alarms = Alarms
		self.updaters =updaters

		self.numMessages = 0
		self.t1 = time.clock()
		self.messages = [0, 0]
		self.t = [0, 0]
		self.timing = False

		self.waveSculptorFrame = ttk.Labelframe(self.parent, text='Wave Sculptor')
		self.waveSculptorFrame.propagate(0)

		self.waveSculptorFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.waveSculptorFig, master=self.waveSculptorFrame)
		self.canvas.get_tk_widget().pack()
		self.waveSculptorPlot = self.waveSculptorFig.add_subplot(111)
		self.waveLine, = self.waveSculptorPlot.plot(self.t, self.messages, 'm-')
		self.waveSculptorPlot.set_xlim([0, 300])
		self.waveSculptorPlot.set_ylim([0, 100])
		self.waveSculptorPlot.invert_xaxis()
		self.waveSculptorFig.subplots_adjust(bottom=.15, right=.95)

		self.records = []

		self.names = ['motid',
						'motflag',
						'motbus',
						'motvel',
						'motphasecurrent',
						'motvoltvector',
						'motcurrentvector',
						'motbackemf',
						'motrail0',
						'motrail1',
						'mottemp0',
						'mottemp1',
						'motodo',
						'motslip',
						'motid2',
						'motflag2',
						'motbus2',
						'motvel2',
						'motphasecurrent2',
						'motvoltvector2',
						'motcurrentvector2',
						'motbackemf2',
						'motrail0_2',
						'motrail1_2',
						'mottemp0_2',
						'mottemp1_2',
						'motodo2',
						'motslip2']

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
		self.messages += [self.numMessages/dt]  #messages/s
		if self.numMessages == 0:
			if not self.timing:
				self.timingBegin = time.clock()
				self.timing = True
			elif time.clock() - self.timingBegin > 30:
				if not self.Alarms.alarmPlaying[8]:
					self.Alarms.waveSculptorLabel['foreground'] = 'red'
					self.Alarms.alarmPlaying[8] = True
					waveSculptorAlarm = 'overvolt_alarm.wav'
					waveSculptorAlarmThread = threading.Thread(target=self.Alarms.playAlarm, args=[waveSculptorAlarm, 8])
					waveSculptorAlarmThread.Daemon = True
					waveSculptorAlarmThread.start()
		else:
			self.Alarms.waveSculptorLabel['foreground'] = 'black'
			self.timing = False
			self.Alarms.alarmPlaying[8] = False
		self.numMessages = 0
		if self.t[1] > 300:
			del self.t[0]
			del self.messages[0]
		self.waveLine.set_xdata(self.t)
		self.waveLine.set_ydata(self.messages)
		self.waveSculptorFig.canvas.draw()