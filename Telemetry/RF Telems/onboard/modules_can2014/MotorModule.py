import datetime
import time
import json
import Tkinter as tk 
import ttk
import threading

import data_bus
import wire
import GLOBALS

class MotorModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)

		self.MotorFrame = ttk.Labelframe(self.parent, text='Motor', width=300)

		self.PowerOut = ttk.Label(self.MotorFrame, text='Power Out:', font=('Helvetica', 25))
		self.PowerOut.grid(column=1, row=1)
		self.PowerOutData = ttk.Label(self.MotorFrame, text='N/A', font=('Helvetica', 25), foreground='red')
		self.PowerOutData.grid(column=2, row=1)

		self.Voltage = ttk.Label(self.MotorFrame, text='Voltage:')
		self.Voltage.grid(column=1, row=2)
		self.VoltageData = ttk.Label(self.MotorFrame, text='N/A', width=8)
		self.VoltageData.grid(column=2, row=2)

		self.BackEMF = ttk.Label(self.MotorFrame, text='Back EMF:')
		self.BackEMF.grid(column=1, row=3)
		self.BackEMFData = ttk.Label(self.MotorFrame, text='N/A', width=8)
		self.BackEMFData.grid(column=2, row=3)

		self.MotEff = ttk.Label(self.MotorFrame, text='Mot Eff:')
		self.MotEff.grid(column=3, row=1)
		self.MotEffData = ttk.Label(self.MotorFrame, text='N/A', width=10)
		self.MotEffData.grid(column=4, row=1)

		self.CurrentCS = ttk.Label(self.MotorFrame, text='Current (CS):')
		self.CurrentCS.grid(column=3, row=2)
		self.CurrentCSData = ttk.Label(self.MotorFrame, text='N/A', width=10)
		self.CurrentCSData.grid(column=4, row=2)

		self.CurrentMC = ttk.Label(self.MotorFrame, text='Current (MC):')
		self.CurrentMC.grid(column=3, row=3)
		self.CurrentMCData = ttk.Label(self.MotorFrame, text='N/A', width=10)
		self.CurrentMCData.grid(column=4, row=3)

		self.records = []
		self.harnesses = []
		self.names = [['motcmd'],
						['motbuscmd'],
						['motcmd2'],
						['motbus'],
						['motvel'],
						['motbackemf'],
						['motbus2'],
						['motvel2']]
		self.updaters = [self.leftTorqueUpdater,
							self.leftCurMcUpdater,
							self.rightTorqueUpdater,
							self.leftVoltCurrPowUpdater,
							self.leftMotVelUpdater,
							self.leftBackEmfUpdater,
							self.rightVoltCurrPowUpdater,
							self.rightMotVelUpdater,
							self.rightBackEmfUpdater]
		self.leftPower = 0
		self.leftTorque = 0
		self.leftMotAngVel = 0
		self.leftEff = 0
		self.leftVolt = 0
		self.leftCur = 0
		self.leftBackEmf = 0

		self.rightPower = 0
		self.rightTorque = 0
		self.rightMotAngVel = 0
		self.rightEff = 0
		self.rightVolt = 0
		self.rightCur = 0
		self.rightBackEmf = 0

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
		elif name == ['motbackemf2']:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.records += [record]
			self.harnesses += [harness]
			for i in range(len(self.records)):
				self.records[i].value_callback.append(self.updaters[i])
				self.records[i].Subscribe()

	def leftTorqueUpdater(self, record):  #motcmd
		self.harnesses[0].buf = buffer(record.value)
		self.leftTorque = self.harnesses[0]['trq'].value 
		self.calcEfficiency(1)

	def leftCurMcUpdater(self, record):  #motbuscmd
		self.harnesses[1].buf = buffer(record.value)
		self.CurrentMCData['text'] = "%.2f" % self.harnesses[1]['cur'].value

	def rightTorqueUpdater(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.rightTorque = self.harnesses[2]['trq'].value
		self.calcEfficiency(0)

	def leftVoltCurrPowUpdater(self, record):  #motbus
		self.harnesses[3].buf = buffer(record.value)
		self.leftCur = self.harnesses[3]['curr'].value
		self.leftVolt = self.harnesses[3]['volt'].value
		self.leftPower = self.leftCur*self.leftVolt
		self.CurrentCSData['text'] = "%.2f" % (.5*(self.leftCur + self.rightCur))
		self.VoltageData['text'] = "%.2f" % (.5*(self.leftVolt + self.rightVolt))
		self.PowerOutData['text'] = "%.2f" % (self.leftPower + self.rightPower)

	def leftMotVelUpdater(self, record):  #motvel;
		self.harnesses[4].buf = buffer(record.value)
		self.leftMotAngVel = self.harnesses[4]['motvel'].value
		self.calcEfficiency(1)

	def leftBackEmfUpdater(self, record):  #motbackemf
		self.harnesses[5].buf = buffer(record.value)
		self.leftBackEmf = self.harnesses[5]['bemfq'].value 
		self.BackEMFData['text'] = "%.2f" % (.5*(self.leftBackEmf + self.rightBackEmf))

	def rightVoltCurrPowUpdater(self, record):
		self.harnesses[6].buf = buffer(record.value)
		self.rightCur = self.harnesses[6]['curr'].value 
		self.rightVolt = self.harnesses[6]['volt'].value 
		self.rightPower = self.rightCur*self.rightVolt
		self.CurrentCSData['text'] = '%.2f' % (.5*(self.leftCur + self.rightCur))
		self.VoltageData['text'] = '%.2f' % (.5*(self.leftVolt + self.rightVolt))
		self.PowerOutData['text'] = '%.2f' % (self.leftPower + self.rightPower)

	def rightMotVelUpdater(self, record):
		self.harnesses[7].buf = buffer(record.value)
		self.rightMotAngVel = self.harnesses[7]['motvel'].value 
		self.calcEfficiency(0)

	def rightBackEmfUpdater(self, record):
		self.harnesses[8].buf = buffer(record.value)
		self.rightBackEmf = self.harnesses[8]['bemfq'].value 
		self.BackEMFData['text'] = '%.2f' % (.5*(self.leftBackEmf + self.rightBackEmf))

	def calcEfficiency(self, left):
		if left:
			if self.leftTorque*self.leftMotAngVel == 0:
				self.leftEff = 0
			else:
				self.leftEff = (self.leftMotAngVel/(self.leftMotAngVel + GLOBALS.MOTOR_CONSTANT*self.leftTorque)*100)
		else:
			if self.rightTorque*self.rightMotAngVel == 0:
				self.rightEff = 0
			else:
				self.rightEff = (self.rightMotAngVel/(self.rightMotAngVel + GLOBALS.MOTOR_CONSTANT*self.rightTorque)*100)
		self.MotEffData['text'] = '%.2f' % (.5*(self.leftEff + self.rightEff))

