import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading

import data_bus
import wire

class ArrayUpdater(object):
	def __init__(self, record, harness, Current):
		self.record = record
		self.harness = harness
		self.Current = Current
		self.record.value_callback.append(self.HandleValue)
		self.record.Subscribe()

	def HandleValue(self, record):
		self.harness.buf = buffer(record.value)
		self.Current['text'] = "%.2f" % self.harness['array'].value

class ArrayModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)

		self.ArrayFrame = ttk.Labelframe(self.parent, text='Array', width=250)

		self.PowerIn = ttk.Label(self.ArrayFrame, text='Power In: ', font=('Helvetica',36))
		self.PowerIn.grid(column=1, row=1)
		self.PowerInData = ttk.Label(self.ArrayFrame, text='N/A', font=('Helvetica',36), foreground='green', width=4)
		self.PowerInData.grid(column=2, row=1)

		self.Current = ttk.Label(self.ArrayFrame, text='Current: ')
		self.Current.grid(column=3, row=1)
		self.CurrentData = ttk.Label(self.ArrayFrame, text='N/A', width=7)
		self.CurrentData.grid(column=4, row=1)

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		names = fields[0].split('.')
		if names == ['cur0']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.updaterThread = threading.Thread(target=ArrayUpdater, args=(record, harness, self.CurrentData))
			self.updaterThread.daemon = True
			self.updaterThread.start()