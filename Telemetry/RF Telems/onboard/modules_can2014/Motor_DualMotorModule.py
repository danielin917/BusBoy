import json
import Tkinter as tk 
import ttk

import data_bus
import wire
import GLOBALS

class DualMotorModule(ttk.Frame):
	def __init__(self, root, parent, data_bus):
		self.root = root
		self.parent = parent
		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.handleNewRecord)

		self.dualMotorFrame = ttk.LabelFrame(self.parent, text='Dual Motor Info')

		self.leftLabel = ttk.Label(self.dualMotorFrame, text='Left Motor', font=('helvetica', 17), foreground='red')
		self.leftLabel.grid(row=1, column=1)

		self.leftPowerOut = ttk.Label(self.dualMotorFrame, text='Power Out:')
		self.leftPowerOut.grid(row=2, column=1)
		self.leftPowerOutData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.leftPowerOutData.grid(row=2, column=2)

		self.leftVoltage = ttk.Label(self.dualMotorFrame, text='Voltage:')
		self.leftVoltage.grid(row=3, column=1)
		self.leftVoltageData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.leftVoltageData.grid(row=3, column=2)

		self.leftBackEmf = ttk.Label(self.dualMotorFrame, text='Back EMF:')
		self.leftBackEmf.grid(row=4, column=1)
		self.leftBackEmfData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.leftBackEmfData.grid(row=4, column=2)

		self.leftMotEff = ttk.Label(self.dualMotorFrame, text='Mot Eff:')
		self.leftMotEff.grid(row=2, column=3)
		self.leftMotEffData = ttk.Label(self.dualMotorFrame, text='N/A', width=4)
		self.leftMotEffData.grid(row=2, column=4)

		self.leftCurrentCS = ttk.Label(self.dualMotorFrame, text='Current (CS):')
		self.leftCurrentCS.grid(row=3, column=3)
		self.leftCurrentCSData = ttk.Label(self.dualMotorFrame, text='N/A', width=4)
		self.leftCurrentCSData.grid(row=3, column=4)

		self.leftCurrentMC = ttk.Label(self.dualMotorFrame, text='Current (MC):')
		self.leftCurrentMC.grid(row=4, column=3)
		self.leftCurrentMCData = ttk.Label(self.dualMotorFrame, text='N/A', width=4)
		self.leftCurrentMCData.grid(row=4, column=4)

		self.rightLabel = ttk.Label(self.dualMotorFrame, text='Right Motor', font=('helvetica', 17), foreground='green')
		self.rightLabel.grid(row=1, column=5)

		self.rightPowerOut = ttk.Label(self.dualMotorFrame, text='Power Out:')
		self.rightPowerOut.grid(row=2, column=5)
		self.rightPowerOutData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.rightPowerOutData.grid(row=2, column=6)

		self.rightVoltage = ttk.Label(self.dualMotorFrame, text='Voltage:')
		self.rightVoltage.grid(row=3, column=5)
		self.rightVoltageData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.rightVoltageData.grid(row=3, column=6)

		self.rightBackEmf = ttk.Label(self.dualMotorFrame, text='Back EMF:')
		self.rightBackEmf.grid(row=4, column=5)
		self.rightBackEmfData = ttk.Label(self.dualMotorFrame, text='N/A', width=5)
		self.rightBackEmfData.grid(row=4, column=6)

		self.rightMotEff = ttk.Label(self.dualMotorFrame, text='Mot Eff:')
		self.rightMotEff.grid(row=2, column=7)
		self.rightMotEffData = ttk.Label(self.dualMotorFrame, text='N/A')
		self.rightMotEffData.grid(row=2, column=8)

		self.rightCurrentCS = ttk.Label(self.dualMotorFrame, text='Current (CS):')
		self.rightCurrentCS.grid(row=3, column=7)
		self.rightCurrentCSData = ttk.Label(self.dualMotorFrame, text='N/A')
		self.rightCurrentCSData.grid(row=3, column=8)

		self.rightCurrentMC = ttk.Label(self.dualMotorFrame, text='Current (MC):')
		self.rightCurrentMC.grid(row=4, column=7)
		self.rightCurrentMCData = ttk.Label(self.dualMotorFrame, text='N/A')
		self.rightCurrentMCData.grid(row=4, column=8)

		self.harnesses = []
		self.records = []

		self.names = [['motcmd'],
						['motbuscmd'],
						['motcmd2'],
						['motbus'],
						['motvel'],
						['motbackemf'],
						['motbus2'],
						['motvel2']]

		self.updaters = [self.leftTorqueUpdater,
							self.leftCurMCUpdater,
							self.rightTorqueUpdater,
							self.leftVoltCurPowUpdater,
							self.leftMotAngVelUdater,
							self.leftBackEmfUpdater,
							self.rightVoltCurPowUpdater,
							self.rightMotAngVelUpdater,
							self.rightBackEmfUpdater]

		self.leftTorque = 0
		self.leftMotAngVel = 0
		self.rightTorque = 0
		self.rightMotAngVel = 0

	def handleNewRecord(self, record):
		fields = record.field.split('\0')
		name = fields[0].split('.')
		if name in self.names:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
		elif name == ['motbackemf2']:
			meta = json.loads(fields[1])
			self.harnesses += [wire.Harness(meta['harness'])]
			self.records += [record]
			for i in range(len(self.records)):
				self.records[i].value_callback.append(self.updaters[i])
				self.records[i].Subscribe()

	def leftTorqueUpdater(self, record):
		self.harnesses[0].buf = buffer(record.value)
		self.leftTorque = self.harnesses[0]['trq'].value
		self.calcEfficiency(1)

	def leftCurMCUpdater(self, record):
		self.harnesses[1].buf = buffer(record.value)
		self.leftCurrentMCData['text'] = '%.2f' % self.harnesses[1]['cur'].value

	def rightTorqueUpdater(self, record):
		self.harnesses[2].buf = buffer(record.value)
		self.rightTorque = self.harnesses[2]['trq'].value 
		self.calcEfficiency(0)

	def leftVoltCurPowUpdater(self, record):
		self.harnesses[3].buf = buffer(record.value)
		self.leftVoltageData['text'] = '%.2f' % self.harnesses[3]['volt'].value 
		self.leftCurrentCSData['text'] = '%.2f' % self.harnesses[3]['curr'].value 
		self.leftPowerOutData['text'] = '%.2f' % (self.harnesses[3]['volt'].value*self.harnesses[3]['curr'].value)

	def leftMotAngVelUdater(self, record):
		self.harnesses[4].buf = buffer(record.value)
		self.leftMotAngVel = self.harnesses[4]['motvel'].value
		self.calcEfficiency(1)

	def leftBackEmfUpdater(self, record):
		self.harnesses[5].buf = buffer(record.value)
		self.leftBackEmfData['text'] = '%.2f' % self.harnesses[5]['bemfq'].value

	def rightVoltCurPowUpdater(self, record):
		self.harnesses[6].buf = buffer(record.value)
		self.rightVoltageData['text'] = '%.2f' % self.harnesses[6]['volt'].value 
		self.rightCurrentCSData['text'] = '%.2f' % self.harnesses[6]['curr'].value 
		self.rightPowerOutData['text'] = '%.2f' % (self.harnesses[6]['volt'].value*self.harnesses[6]['curr'].value) 

	def rightMotAngVelUpdater(self, record):
		self.harnesses[7].buf = buffer(record.value)
		self.rightMotAngVel = self.harnesses[7]['motvel'].value 
		self.calcEfficiency(0)

	def rightBackEmfUpdater(self, record):
		self.harnesses[8].buf = buffer(record.value)
		self.rightBackEmfData['text'] = '%.2f' % self.harnesses[8]['bemfq'].value

	def calcEfficiency(self, left):
		if left:
			if self.leftTorque*self.leftMotAngVel == 0:
				self.leftMotEffData['text'] = 0
			else:
				self.leftMotEffData['text'] = '%.2f' % (self.leftMotAngVel/(self.leftMotAngVel + GLOBALS.MOTOR_CONSTANT*self.leftTorque)*100)
		else:
			if self.rightTorque*self.rightMotAngVel == 0:
				self.rightMotEffData['text'] = 0
			else:
				self.rightMotEffData['text'] = '%.2f' % (self.rightMotAngVel/(self.rightMotAngVel + GLOBALS.MOTOR_CONSTANT*self.rightTorque)*100)