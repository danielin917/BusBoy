import datetime
import time
import json
import Tkinter as tk 
import ttk
import matplotlib.pyplot as plt 
import matplotlib.backends.backend_tkagg as tkagg 

import data_bus
import wire

class Strat_SolarRadiationModule(ttk.Frame):
	def __init__(self, parent, data_bus, updaters):
		self.parent = parent
		self.data_bus = data_bus
		self.updaters = updaters

		self.solarRadiationFrame = ttk.Labelframe(self.parent, text='Solar Radiation Graph')

		self.solarRadFig = plt.Figure()
		self.canvas = tkagg.FigureCanvasTkAgg(self.solarRadFig, master=self.solarRadiationFrame)
		self.canvas.get_tk_widget().pack()
		self.solarRadPlot = self.solarRadFig.add_subplot(111)

	def addResetMark(self):
		pass