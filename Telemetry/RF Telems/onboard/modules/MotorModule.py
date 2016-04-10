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
	def __init__(self, parent, data_bus):
		self.parent = parent
		self.data_bus = data_bus

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

		self.updatersDict = {'motcmd' : self.leftTorqueUpdater,
							'motbuscmd' : self.leftCurMcUpdater,
							'motcmd2' : self.rightTorqueUpdater,
							'motbus' : self.leftVoltCurrPowUpdater,
							'motvel' : self.leftMotVelUpdater,
							'motbackemf' : self.leftBackEmfUpdater,
							'motbus2' : self.rightVoltCurrPowUpdater,
							'motvel2' : self.rightMotVelUpdater,
							'motbackemf2' : self.rightBackEmfUpdater,
							'micro_current' : self.currentUpdater}
		self.harnesses = {}

		self.leftPower = 0
		self.leftTorque = 0
		self.leftMotAngVel = 0
		self.leftEff = 0
		self.leftVolt = 0
		self.leftBackEmf = 0

		self.rightPower = 0
		self.rightTorque = 0
		self.rightMotAngVel = 0
		self.rightEff = 0
		self.rightVolt = 0
		self.rightBackEmf = 0

		self.current = 0

		self.data_bus.record_callback.append(self.HandleNewRecord)

	def HandleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0]
		if name in self.updatersDict:
			meta = json.loads(fields[1])
			harness = wire.Harness(meta['harness'])
			self.harnesses[name] = harness
			record.value_callback.append(self.updatersDict[name])
			record.Subscribe()

	def currentUpdater(self, record):
		self.harnesses['micro_current'].buf = buffer(record.value)
		self.current = self.harnesses['micro_current']['motor'].value 
		self.CurrentCSData['text'] = "%.2f" % self.current

	def leftTorqueUpdater(self, record):  #motcmd
		self.harnesses['motcmd'].buf = buffer(record.value)
		self.leftTorque = self.harnesses['motcmd']['trq'].value 
		self.calcLeftEfficiency()

	def leftCurMcUpdater(self, record):  #motbuscmd
		self.harnesses['motbuscmd'].buf = buffer(record.value)
		self.CurrentMCData['text'] = "%.2f" % self.harnesses['motbuscmd']['cur'].value

	def rightTorqueUpdater(self, record): #motcmd2
		self.harnesses['motcmd2'].buf = buffer(record.value)
		self.rightTorque = self.harnesses['motcmd2']['trq'].value
		self.calcRightEfficiency()

	def leftVoltCurrPowUpdater(self, record):  #motbus
		self.harnesses['motbus'].buf = buffer(record.value)
		self.leftVolt = self.harnesses['motbus']['volt'].value
		self.leftPower = self.current*self.leftVolt*.5
		self.VoltageData['text'] = "%.3f" % (.5*(self.leftVolt + self.rightVolt))
		self.PowerOutData['text'] = "%.2f" % (self.leftPower + self.rightPower)

	def leftMotVelUpdater(self, record):  #motvel;
		self.harnesses['motvel'].buf = buffer(record.value)
		self.leftMotAngVel = self.harnesses['motvel']['motvel'].value
		self.calcLeftEfficiency()

	def leftBackEmfUpdater(self, record):  #motbackemf
		self.harnesses['motbackemf'].buf = buffer(record.value)
		self.leftBackEmf = self.harnesses['motbackemf']['bemfq'].value 
		self.BackEMFData['text'] = "%.2f" % (.5*(self.leftBackEmf + self.rightBackEmf))

	def rightVoltCurrPowUpdater(self, record):
		self.harnesses['motbus2'].buf = buffer(record.value)
		self.rightVolt = self.harnesses['motbus2']['volt'].value 
		self.rightPower = self.current*self.rightVolt*.5
		self.VoltageData['text'] = '%.3f' % (.5*(self.leftVolt + self.rightVolt))
		self.PowerOutData['text'] = '%.2f' % (self.leftPower + self.rightPower)

	def rightMotVelUpdater(self, record):
		self.harnesses['motvel2'].buf = buffer(record.value)
		self.rightMotAngVel = self.harnesses['motvel2']['motvel'].value 
		self.calcRightEfficiency()

	def rightBackEmfUpdater(self, record):
		self.harnesses['motbackemf2'].buf = buffer(record.value)
		self.rightBackEmf = self.harnesses['motbackemf2']['bemfq'].value 
		self.BackEMFData['text'] = '%.2f' % (.5*(self.leftBackEmf + self.rightBackEmf))

	def calcLeftEfficiency(self):
		if self.leftTorque*self.leftMotAngVel == 0:
			self.leftEff = 0
		else:
			self.leftEff = (self.leftMotAngVel/(self.leftMotAngVel + GLOBALS.MOTOR_CONSTANT*self.leftTorque)*100)
		self.MotEffData['text'] = '%.2f' % (.5*(self.leftEff + self.rightEff))

	def calcRightEfficiency(self):
		if self.rightTorque*self.rightMotAngVel == 0:
			self.rightEff = 0
		else:
			self.rightEff = (self.rightMotAngVel/(self.rightMotAngVel + GLOBALS.MOTOR_CONSTANT*self.rightTorque)*100)
		self.MotEffData['text'] = '%.2f' % (.5*(self.leftEff + self.rightEff))
