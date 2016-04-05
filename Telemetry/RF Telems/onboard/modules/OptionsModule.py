import Tkinter as tk 
import ttk

class Options(ttk.Frame):
	def __init__(self, parent):
		self.parent = parent
		
		self.Options = ttk.Frame(self.parent)

		self.database = ttk.Label(self.Options, text='Database')
		self.database.grid(row=1, column=1)
		self.databaseEntry = ttk.Entry(self.Options)
		self.databaseEntry.grid(row=1, column=2)
		self.setDatabase = ttk.Button(self.Options, text='Set')
		self.setDatabase.grid(row=1, column=3)
		self.reconnect = ttk.Button(self.Options, text='Reconnect to Database')
		self.reconnect.grid(row=2, column=1)

		self.anemometerVisual = ttk.Label(self.Options, text='Anemometer Visual')
		self.anemometerVisual.grid(row=3, column=1)

		self.headingVar = tk.IntVar()
		self.windHeading = ttk.Radiobutton(self.Options, text='Wind heading', variable=self.headingVar, value=1)
		self.windHeading.grid(row=4, column=1)		
		self.airHeading = ttk.Radiobutton(self.Options, text='Air heading', variable=self.headingVar, value=0)
		self.airHeading.grid(row=4, column=2)

		self.speedVar = tk.IntVar()
		self.windSpeed = ttk.Radiobutton(self.Options, text='Wind speed', variable=self.speedVar, value=0)
		self.windSpeed.grid(row=5, column=1)
		self.airSpeed = ttk.Radiobutton(self.Options, text='Air speed', variable=self.speedVar, value=1)
		self.airSpeed.grid(row=5, column=2)

		self.unitsVar = tk.IntVar()
		self.unitsVar.set(0)
		self.speedUnits = ttk.Label(self.Options, text='Speed Units: ')
		self.speedUnits.grid(row=6, column=1)
		self.mph = ttk.Radiobutton(self.Options, text='MPH', variable=self.unitsVar, value=0)
		self.mph.grid(row=6, column=2)
		self.kph = ttk.Radiobutton(self.Options, text='KPH', variable=self.unitsVar, value=1)
		self.kph.grid(row=6, column=3)
		self.mps = ttk.Radiobutton(self.Options, text='m/s', variable=self.unitsVar, value=2)
		self.mps.grid(row=6, column=4)

		self.packType = tk.BooleanVar()
		#self.packType = False
		self.MainPack = ttk.Radiobutton(self.Options, text='Main Pack', variable=self.packType, value=False)
		self.MainPack.grid(row=7, column=2)
		self.FullPack = ttk.Radiobutton(self.Options, text='Full Pack', variable=self.packType, value=True)
		self.FullPack.grid(row=7, column=3)
		# self.airSpeed = ttk.Radiobutton(self.Options, text='Air speed', variable=self.packType, value=False)
		# self.airSpeed.grid(row=5, column=2)

	def updateStuff(self):
		pass