import datetime
import time
import json
import Tkinter as tk 
import ttk
from Tkinter import Canvas
import numpy as np
import threading

import data_bus
import wire

def updateCompass(x, y, r, angle, canvas):
	radAngle = angle*np.pi/180
	return canvas.create_line(x, y, x + r*np.sin(radAngle), y - r*np.cos(radAngle), width=3, arrow=tk.LAST)

class AnemometerUpdater(object):
	def __init__(self, record, harness, needle, canvas):
		self.record = record
		self.harness = harness
		self.needle = needle
		self.canvas = canvas
		self.record.value_callback.append(self.handleValue)

	def handleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.canvas.delete(self.needle)
		self.needle = updateCompass(87, 87, 80, self.harness['current'].value, self.canvas) 

class Strat_AnemometerModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.anemometerFrame = ttk.Labelframe(self.parent, text='Anemometer')
		self.mainPane = ttk.Panedwindow(self.anemometerFrame, orient=tk.VERTICAL)
		self.mainPane.pack()
		self.topPane = ttk.Frame(self.mainPane, height=180)
		self.bottomPane = ttk.Frame(self.mainPane, height=120)
		self.topPane.pack()
		self.bottomPane.pack()

		self.canvas = Canvas(self.topPane, height = 180, width=180)#, height=300, width=180)
		outerCircle = self.createCircle(87,87,80)
		innerCircle = self.createCircle(87,87,40)
		self.needle = updateCompass(87,87,80,0,self.canvas)

		self.wind = ttk.Label(self.bottomPane, text='Wind: ')
		self.wind.grid(row=1, column=1)
		self.windSpeed = ttk.Label(self.bottomPane, text='0.00')
		self.windSpeed.grid(row=1, column=2)
		self.windmph = ttk.Label(self.bottomPane, text='mph ')
		self.windmph.grid(row=1, column=3)
		self.windDir = ttk.Label(self.bottomPane, text='0.00')
		self.windDir.grid(row=1, column=4)
		self.windDegree = ttk.Label(self.bottomPane, text=u'\xb0')
		self.windDegree.grid(row=1, column=5)

		self.predicted = ttk.Label(self.bottomPane, text='Predicted: ')
		self.predicted.grid(row=2, column=1)
		self.predictedSpeed = ttk.Label(self.bottomPane, text='0.00')
		self.predictedSpeed.grid(row=2, column=2)
		self.predictedmph = ttk.Label(self.bottomPane, text='mph ')
		self.predictedmph.grid(row=2, column=3)
		self.predictedDir = ttk.Label(self.bottomPane, text='0.00')
		self.predictedDir.grid(row=2, column=4)
		self.predictedDegree = ttk.Label(self.bottomPane, text=u'\xb0')
		self.predictedDegree.grid(row=2, column=5)

		self.car = ttk.Label(self.bottomPane, text='Car: ')
		self.car.grid(row=3, column=1)
		self.carSpeed = ttk.Label(self.bottomPane, text='0.00')
		self.carSpeed.grid(row=3, column=2)
		self.carmph = ttk.Label(self.bottomPane, text='mph ')
		self.carmph.grid(row=3, column=3)
		self.carDir = ttk.Label(self.bottomPane, text='0.00')
		self.carDir.grid(row=3, column=4)
		self.carDegree = ttk.Label(self.bottomPane, text=u'\xb0')
		self.carDegree.grid(row=3, column=5)

		self.canvas.pack()

	def createCircle(self, x, y, r, **kwargs):
		return self.canvas.create_oval(x-r, y-r, x+r, y+r, **kwargs)

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['bms_data']:        #this is not the actual record for anemometer data. just a placeholder to make sure the compass update works
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=AnemometerUpdater, args=(record, harness, self.needle, self.canvas))
			self.updaterThread.daemon = True
			self.updaterThread.start()
