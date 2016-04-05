import json
import Tkinter as tk
import ttk
import threading
import time
import numpy as np

import data_bus
import wire
import GLOBALS


class BatteryTab(ttk.Panedwindow):
    def __init__(self, parent, data_bus):
        self.parent = parent
        self.data_bus = data_bus

        ttk.Panedwindow.__init__(self, self.parent, orient=tk.VERTICAL)

        self.battVoltFrame = ttk.Labelframe(self, text='Voltages', height=150)
        self.add(self.battVoltFrame)

        self.battTempFrame = ttk.Labelframe(self, text='Temps', height=150)
        self.add(self.battTempFrame)

        self.battVolts = []
        self.battVoltTexts = []
        self.battTemps = []
        self.battTempTexts = []

        self.battVoltInit()
        self.battTempInit()

        self.harnesses = {}

        self.updatersDict = {'bms_volts_0' : self.volt0Updater,
                            'bms_volts_1' : self.volt1Updater,
                            'bms_volts_2' : self.volt2Updater,
                            'bms_volts_3' : self.volt3Updater,
                            'bms_volts_4' : self.volt4Updater,
                            'bms_volts_5' : self.volt5Updater,
                            'bms_volts_6' : self.volt6Updater,
                            'bms_volts_7' : self.volt7Updater,
                            'bms_volts_8' : self.volt8Updater,
                            'bms_volts_9' : self.volt9Updater,
                            'bms_volts_10' : self.volt10Updater,
                            'bms_temp_0' : self.temp0Updater,
                            'bms_temp_1' : self.temp1Updater,
                            'bms_temp_2' : self.temp2Updater,
                            'bms_temp_3' : self.temp3Updater,
                            'bms_temp_4' : self.temp4Updater,
                            'bms_temp_5' : self.temp5Updater,
                            'bms_temp_6' : self.temp6Updater,
                            'bms_temp_7' : self.temp7Updater,
                            'bms_temp_8' : self.temp8Updater,
                            'bms_temp_9' : self.temp9Updater,
                            'bms_temp_10' : self.temp10Updater}

        self.T_min = 20

        self.data_bus.record_callback.append(self.handleNewRecord)

    def updateStuff(self):
        self.updateTempColors()
        self.updateVoltColors()

    def battVoltInit(self):
        for i in range(21):
            self.battVolts += [0]
            modNum = ttk.Label(self.battVoltFrame, text=str(i))  #, width=3)
            modNum.grid(row=1, column=i)
            voltText = ttk.Label(self.battVoltFrame, text='N/A', width=5, font=('helvetica', 16))
            voltText.grid(row=2, column=i)
            self.battVoltTexts += [voltText]
        for j in range(21, 43):
            self.battVolts += [0]
            modNum = ttk.Label(self.battVoltFrame, text=str(j))
            modNum.grid(row=3, column=(j - 21))
            voltText = ttk.Label(self.battVoltFrame, text='N/A', width=5, font=('helvetica', 16))
            voltText.grid(row=4, column=(j - 21))
            self.battVoltTexts += [voltText]

    def battTempInit(self):
        for i in range(21):
            self.battTemps += [0]
            modNum = ttk.Label(self.battTempFrame, text=str(i))
            modNum.grid(row=1, column=i)
            tempText = ttk.Label(self.battTempFrame, text='N/A', width=5, font=('helvetica', 16))
            tempText.grid(row=2, column=i)
            self.battTempTexts += [tempText]
        for j in range(21, 43):
            self.battTemps += [0]
            modNum = ttk.Label(self.battTempFrame, text=str(j))
            modNum.grid(row=3, column=(j - 21))
            tempText = ttk.Label(self.battTempFrame, text='N/A', width=5, font=('helvetica', 16))
            tempText.grid(row=4, column=(j - 21))
            self.battTempTexts += [tempText]

    def handleNewRecord(self, record):
        fields = record.field.split('\0')
        name = fields[0]
        if name in self.updatersDict:
            meta = json.loads(fields[1])
            harness = wire.Harness(meta['harness'])
            self.harnesses[name] = harness
            record.value_callback.append(self.updatersDict[name])
            record.Subscribe()

    def volt0Updater(self, record):
        self.harnesses['bms_volts_0'].buf = buffer(record.value)
        for i in range(0, 4):
            self.battVolts[i] = self.harnesses['bms_volts_0']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt1Updater(self, record):
        self.harnesses['bms_volts_1'].buf = buffer(record.value)
        for i in range(4, 8):
            self.battVolts[i] = self.harnesses['bms_volts_1']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt2Updater(self, record):
        self.harnesses['bms_volts_2'].buf = buffer(record.value)
        for i in range(8, 12):
            self.battVolts[i] = self.harnesses['bms_volts_2']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt3Updater(self, record):
        self.harnesses['bms_volts_3'].buf = buffer(record.value)
        for i in range(12, 16):
            self.battVolts[i] = self.harnesses['bms_volts_3']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt4Updater(self, record):
        self.harnesses['bms_volts_4'].buf = buffer(record.value)
        for i in range(16, 20):
            self.battVolts[i] = self.harnesses['bms_volts_4']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt5Updater(self, record):
        self.harnesses['bms_volts_5'].buf = buffer(record.value)
        for i in range(20, 24):
            self.battVolts[i] = self.harnesses['bms_volts_5']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt6Updater(self, record):
        self.harnesses['bms_volts_6'].buf = buffer(record.value)
        for i in range(24, 28):
            self.battVolts[i] = self.harnesses['bms_volts_6']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt7Updater(self, record):
        self.harnesses['bms_volts_7'].buf = buffer(record.value)
        for i in range(28, 32):
            self.battVolts[i] = self.harnesses['bms_volts_7']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt8Updater(self, record):
        self.harnesses['bms_volts_8'].buf = buffer(record.value)
        for i in range(32, 36):
            self.battVolts[i] = self.harnesses['bms_volts_8']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]

    def volt9Updater(self, record):
        self.harnesses['bms_volts_9'].buf = buffer(record.value)
        for i in range(36, 40):
            self.battVolts[i] = self.harnesses['bms_volts_9']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]


    def volt10Updater(self, record):
        self.harnesses['bms_volts_10'].buf = buffer(record.value)
        for i in range(40, 43):
            self.battVolts[i] = self.harnesses['bms_volts_10']['volt_' + str(i)].value
            self.battVoltTexts[i]['text'] = '%.3f' % self.battVolts[i]


    def temp0Updater(self, record):
        self.harnesses['bms_temp_0'].buf = buffer(record.value)
        for i in range(0, 4):
            self.battTemps[i] = self.harnesses['bms_temp_0']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.3f' % self.battTemps[i]


    def temp1Updater(self, record):
        self.harnesses['bms_temp_1'].buf = buffer(record.value)
        for i in range(4, 8):
            self.battTemps[i] = self.harnesses['bms_temp_1']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp2Updater(self, record):
        self.harnesses['bms_temp_2'].buf = buffer(record.value)
        for i in range(8, 12):
            self.battTemps[i] = self.harnesses['bms_temp_2']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp3Updater(self, record):
        self.harnesses['bms_temp_3'].buf = buffer(record.value)
        for i in range(12, 16):
            self.battTemps[i] = self.harnesses['bms_temp_3']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp4Updater(self, record):
        self.harnesses['bms_temp_4'].buf = buffer(record.value)
        for i in range(16, 20):
            self.battTemps[i] = self.harnesses['bms_temp_4']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp5Updater(self, record):
        self.harnesses['bms_temp_5'].buf = buffer(record.value)
        for i in range(20, 24):
            self.battTemps[i] = self.harnesses['bms_temp_5']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp6Updater(self, record):
        self.harnesses['bms_temp_6'].buf = buffer(record.value)
        for i in range(24, 28):
            self.battTemps[i] = self.harnesses['bms_temp_6']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp7Updater(self, record):
        self.harnesses['bms_temp_7'].buf = buffer(record.value)
        for i in range(28, 32):
            self.battTemps[i] = self.harnesses['bms_temp_7']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp8Updater(self, record):
        self.harnesses['bms_temp_8'].buf = buffer(record.value)
        for i in range(32, 36):
            self.battTemps[i] = self.harnesses['bms_temp_8']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp9Updater(self, record):
        self.harnesses['bms_temp_9'].buf = buffer(record.value)
        for i in range(36, 40):
            self.battTemps[i] = self.harnesses['bms_temp_9']['temp_' + str(i)].value
            self.battTempTexts[i]['text'] = '%.2f' % self.battTemps[i]


    def temp10Updater(self, record):
        self.harnesses['bms_temp_10'].buf = buffer(record.value)
        self.battTemps[40] = self.harnesses['bms_temp_10']['temp_' + str(40)].value
        self.battTempTexts[40]['text'] = '%.2f' % self.battTemps[40]


    def updateVoltColors(self):
        voltRange = []
        Ids = []
        for i in range(len(self.battVolts)):
            if self.battVolts[i] < GLOBALS.BATT_UNDERVOLT_THRESHOLD:
                self.battVoltTexts[i]['foreground'] = 'black'
            elif self.battVolts[i] > GLOBALS.BATT_OVERVOLT_THRESHOLD:
                self.battVoltTexts[i]['foreground'] = 'black'
            else:
                voltRange += [self.battVolts[i]]
                Ids += [i]
        if len(voltRange) > 0:
            mean = np.mean(voltRange)
            stdDev = np.std(voltRange)
            j = 0
            while j < len(voltRange):
                if abs(voltRange[j] - mean) > 2 * stdDev:
                    self.battVoltTexts[Ids[j]]['foreground'] = 'black'
                    del voltRange[j]
                    del Ids[j]
                else:
                    j += 1
            voltColorRange = max(voltRange) - min(voltRange)
            if voltColorRange > 0:
                for k in range(len(voltRange)):
                    V_rel = voltRange[k] - min(voltRange)
                    norm = V_rel / voltColorRange
                    redHexVal = int(norm * 4095)
                    blueHexVal = 4095 - redHexVal
                    redHex = hex(redHexVal)
                    blueHex = hex(blueHexVal)
                    for m in range(5 - len(redHex)):
                        redHex = redHex[:2] + '0' + redHex[2:]
                    for n in range(5 - len(blueHex)):
                        blueHex = blueHex[:2] + '0' + blueHex[2:]
                    self.battVoltTexts[Ids[k]]['foreground'] = '#' + redHex[2:] + '000' + blueHex[2:]


    def updateTempColors(self):
        tempRange = []
        Ids = []
        for i in range(len(self.battTemps)):
            if self.battTemps[i] < self.T_min:
                self.battTempTexts[i]['foreground'] = 'black'
            elif self.battTemps[i] > GLOBALS.BATT_OVERTEMP_THRESHOLD:
                self.battTempTexts[i]['foreground'] = 'black'
            else:
                tempRange += [self.battTemps[i]]
                Ids += [i]
        if len(tempRange) > 0:
            mean = np.mean(tempRange)
            stdDev = np.std(tempRange)
            j = 0
            while j < len(tempRange):
                if abs(tempRange[j] - mean) > 2 * stdDev:
                    self.battTempTexts[Ids[j]]['foreground'] = 'black'
                    del tempRange[j]
                    del Ids[j]
                else:
                    j += 1
            tempColorRange = max(tempRange) - min(tempRange)
            if tempColorRange > 0:
                for k in range(len(tempRange)):
                    T_rel = tempRange[k] - min(tempRange)
                    norm = T_rel / tempColorRange
                    redHexVal = int(norm * 4095)
                    blueHexVal = 4095 - redHexVal
                    redHex = hex(redHexVal)
                    blueHex = hex(blueHexVal)
                    for m in range(5 - len(redHex)):
                        redHex = redHex[:2] + '0' + redHex[2:]
                    for n in range(5 - len(blueHex)):
                        blueHex = blueHex[:2] + '0' + blueHex[2:]
                    self.battTempTexts[Ids[k]]['foreground'] = '#' + redHex[2:] + '000' + blueHex[2:]
