import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading
import numpy as np

import data_bus
import wire

class Strat_StatisticsModule(ttk.Frame):
	def __init__(self, parent, data_bus, SOCgraph, PinGraph, PoutGraph, RadGraph):
		self.parent = parent
		self.data_bus = data_bus
		self.SOCgraph = SOCgraph
		self.PinGraph = PinGraph
		self.PoutGraph = PoutGraph
		self.RadGraph = RadGraph

		self.statisticsFrame = ttk.Labelframe(self.parent, text='Statistics')

		self.realData = ttk.Label(self.statisticsFrame, text='Real Data')
		self.realData.grid(row=1, column=1, sticky=tk.W)
		self.simulatedData = ttk.Label(self.statisticsFrame, text='Simulated Data')
		self.simulatedData.grid(row=1, column=3, sticky=tk.E)

		self.realSOCData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realSOCData.grid(row=2, column=1)
		self.realSOC = ttk.Label(self.statisticsFrame, text='SOC')
		self.realSOC.grid(row=2, column=2)
		self.simSOCData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simSOCData.grid(row=2, column=3)
		self.simSOC = ttk.Label(self.statisticsFrame, text='SOC')
		self.simSOC.grid(row=2, column=4)

		self.realPinData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realPinData.grid(row=3, column=1)
		self.realPin = ttk.Label(self.statisticsFrame, text='Pin')
		self.realPin.grid(row=3, column=2)
		self.simPinData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simPinData.grid(row=3, column=3)
		self.simPin = ttk.Label(self.statisticsFrame, text='Pin')
		self.simPin.grid(row=3, column=4)

		self.realPoutData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realPoutData.grid(row=4, column=1)
		self.realPout = ttk.Label(self.statisticsFrame, text='Pout')
		self.realPout.grid(row=4, column=2)
		self.simPoutData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simPoutData.grid(row=4, column=3)
		self.simPout = ttk.Label(self.statisticsFrame, text='Pout')
		self.simPout.grid(row=4, column=4)

		self.realRadData = ttk.Label(self.statisticsFrame, text='N/A')
		self.realRadData.grid(row=5, column=1)
		self.realRad = ttk.Label(self.statisticsFrame, text='Rad')
		self.realRad.grid(row=5, column=2)
		self.simRadData = ttk.Label(self.statisticsFrame, text='N/A')
		self.simRadData.grid(row=5, column=3)
		self.simRad = ttk.Label(self.statisticsFrame, text='Rad')
		self.simRad.grid(row=5, column=4)

		self.resetButton = ttk.Button(self.statisticsFrame, text='Reset Avgs', command=self.reset)
		self.resetButton.grid(row=6, column=2, columnspan=2)

		self.harnesses = {}

		self.batVolt = 0
		self.arrayCur = 0
		self.mot1Pow = 0
		self.mot2Pow = 0
		self.motCurrent = 0
		self.SOCtot = 0
		self.SOCtimeElapsed = 0
		self.SOCtimestamp = time.clock()
		self.PinTimeElapsed = 0
		self.PinTimestamp = time.clock()
		self.PoutTimeElapsed = 0
		self.PoutTimestamp = time.clock()
		self.PinTot = 0
		self.PoutTot = 0
		self.updatersDict = {'bms_pack_volts' : self.handleBatVolt,
							'ab_current' : self.handleArrayCurrent,
							'SOC' : self.handleSOC,
							'motbus' : self.handleMot1,
							'motbus2' : self.handleMot2,
							'micro_current' : self.handleMotorCurrent}

		self.data_bus.record_callback.append(self.handleNewRecord)

	def reset(self):
		self.SOCtot = 0
		self.SOCtimeElapsed = 0
		self.SOCtimestamp = time.clock()
		self.Pintot = 0 
		self.PinTimeElapsed = 0
		self.PinTimestamp = time.clock()
		self.PoutTot = 0
		self.PoutTimeElapsed = 0
		self.PoutTimestamp = time.clock()
		self.SOCgraph.addResetMark()
		self.PinGraph.addResetMark()
		self.PoutGraph.addResetMark()

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			if name == ['bms_pack_volts']:
				self.PinTimestamp = time.clock()
				self.PinTimeElapsed = 0
			if name == ['SOC']:
				self.SOCtimestamp = time.clock()
				self.SOCtimeElapsed = 0
			if name == ['motbus']:
				self.PoutTimestamp = time.clock()
				self.PoutTimeElapsed = 0
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def handleBatVolt(self, record):
		self.harnesses['bms_pack_volts'].buf = buffer(record.value)
		dt = time.clock() - self.PinTimestamp
		self.batVolt = self.harnesses['bms_pack_volts']['pack0'].value
		self.PinTot += self.batVolt*self.arrayCur*dt 
		self.PinTimestamp += dt 
		self.PinTimeElapsed += dt 
		self.realPinData['text'] = '%.2f' % (self.PinTot/self.PinTimeElapsed)

	def handleMotorCurrent(self, record):
		self.harnesses['micro_current'].buf = buffer(record.value)
		self.motCurrent = self.harnesses['micro_current']['motor'].value

	def handleArrayCurrent(self, record):
		self.harnesses['ab_current'].buf = buffer(record.value)
		dt = time.clock() - self.PinTimestamp
		self.arrayCur = self.harnesses['ab_current']['array'].value
		self.PinTot += self.arrayCur*self.batVolt*dt 
		self.PinTimestamp += dt 
		self.PinTimeElapsed += dt 
		self.realPinData['text'] = '%.2f' % (self.PinTot/self.PinTimeElapsed)

	def handleSOC(self, record):
		self.harnesses['SOC'].buf = buffer(record.value)
		dt = time.clock() - self.SOCtimestamp
		self.SOCtot += 100*self.harnesses['SOC']['SOC'].value*dt
		self.SOCtimestamp += dt
		self.SOCtimeElapsed += dt 
		self.realSOCData['text'] = '%.2f' % (self.SOCtot/self.SOCtimeElapsed)

	def handleMot1(self, record):
		self.harnesses['motbus'].buf = buffer(record.value)
		dt = time.clock() - self.PoutTimestamp
		V = self.harnesses['motbus']['volt'].value*.5
		I = self.motCurrent
		self.mot1Pow = V*I 
		self.PoutTot += (self.mot1Pow + self.mot2Pow)*dt
		self.PoutTimestamp += dt 
		self.PoutTimeElapsed += dt 
		self.realPoutData['text'] = '%.2f' % (self.PoutTot/self.PoutTimeElapsed)

	def handleMot2(self, record):
		self.harnesses['motbus2'].buf = buffer(record.value)
		dt = time.clock() - self.PoutTimestamp
		V = self.harnesses['motbus2']['volt'].value*.5
		I = self.motCurrent
		self.mot2Pow = V*I
		self.PoutTot += (self.mot1Pow + self.mot2Pow)*dt
		self.PoutTimestamp += dt 
		self.PoutTimeElapsed += dt 
		self.realPoutData['text'] = '%.2f' % (self.PoutTot/self.PoutTimeElapsed)
