import Tkinter as tk 
import ttk 

class BMSTab(ttk.Frame):
	def __init__(self, parent):
		self.parent = parent
		
		ttk.Frame.__init__(self, self.parent)

	def updateStuff(self):
		pass