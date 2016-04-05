import datetime
import json
import pprint
import time
import Tkinter as tk 
import ttk

import data_bus
import wire

class TextUpdate(object):
	def __init__(self, record, harness, text):
		self.record = record
		self.text = text
		#self.scroll = scroll
		self.harness = harness
		self.record.value_callback.append(self.HandleValue)
		self.record.Subscribe()

	def Unregister(self):
		self.record.value_callback.remove(self.HandleValue)
		self.record.Unsubscribe()

	def HandleValue(self, record):
		self.harness.buf = buffer(record.value)
		for key in self.harness:
			h = self.harness[key]
			self.text.insert("end", h)
			self.text.insert("end", '\n')# + '\n')

class gooey(ttk.Frame):
	def __init__(self, root, data_bus, subscribe_all, *args, **kargs):
		self.frame = ttk.Frame.__init__(self, root, *args, **kargs)

		self.data_bus = data_bus
		self.data_bus.record_callback.append(self.HandleNewRecord)
		self.data_bus.flush_callback.append(self.HandleFlush)

		

		self.subscribe_all = subscribe_all

		self.root = root

		#self.pane = ttk.PanedWindow(self, orient=tk.HORIZONTAL).grid()
		#self.pane.pack(expand=True, fill=tk.BOTH)
        #self.tree_frame = ttk.Frame(self.pane)
        #self.tree_frame.pack(expand=True, fill=Tkinter.Y)
		self.root.grid()
		#self.quitButton = ttk.Button(self, text='quit', command=self.quit)
		#self.quitButton.pack(side=tk.RIGHT, fill=tk.Y)
		self.createWidgets()

		#self.tree = 

		self.records = []


		self.Poll()
		#self.update()

	def HandleNewRecord(self, record):
		record.tree_key = '%i' % len(self.records)
		self.records.append(record)
		fields = record.field.split('\0')
		names = fields[0].split('.')
		self.printtoscreen(names[0])
		#print len(self.records)

	   	#last_parent_path = ""
		#for i in range(len(names)):
		#	parent = names[i]
		#	parent_path = '.'.join(names[0:i+1])
		#	if not self.tree.exists(parent_path):
		#		self.tree.insert(last_parent_path, 'end', parent_path, text=parent)
		#	last_parent_path = parent_path

		#self.tree.insert(last_parent_path, 'end', record.tree_key,
		#				text=record.tree_key)

		record.last_rate_timestamp = time.time()
		record.last_rate_count = 0
		record.value_callback.append(self.update)

 		if self.subscribe_all:
			record.Subscribe()

	def HandleFlush(self):
	 	self.ClearDataPane()
		self.records = []
		children = self.tree.get_children("")
		for c in children:
			self.tree.delete(c)

		pass

	def Poll(self):
		self.data_bus.Poll(time.time(), [],[],[],[])
		self.root.after(20, self.Poll)

	def createWidgets(self):
		self.quitButton = ttk.Button(self, text='quit', command=self.quit)
		self.quitButton.grid()

		self.textbox = tk.Text(self.frame, relief='sunken', state='normal')
		self.textbox.pack(fill='both', expand=1)

		self.yscrollbar = tk.Scrollbar(self.textbox, orient='vertical', cursor='gumby', command=self.textbox.yview)
		self.yscrollbar.pack(side='right', fill='y')
		self.textbox['yscrollcommand']=self.yscrollbar.set

		#self.textbox

	def update(self, record):
		#if (len(self.records) > 0):
			#print 'ya'
		record = record
		fields = record.field.split("\0")
		meta = None
		if len(fields) > 1:
			meta = json.loads(fields[1])
		if meta and 'harness' in meta:
			harness = wire.Harness(meta['harness'])
			self.updater = TextUpdate(record, harness, self.textbox)
			if record.value:
				self.updater.HandleValue(record)

	def printtoscreen(self, content):
		self.textbox.insert("end", content + '\n')


if __name__ == "__main__":
	import argparse
	import os
	import sys
	import tkSimpleDialog

	parser = argparse.ArgumentParser(description='Data Bus viewer.')
	parser.add_argument('connect', metavar='hostname:base_port', type=str, nargs='*', help='Data Buses to connect to.')
	parser.add_argument('-a', '--all', type=bool, default=True, help='Subscribe to all values.')
	args = parser.parse_args()

	root = tk.Tk()
	root.geometry("800x600")
	root.title("the gooey gui")
	program_directory=sys.path[0]
	root.tk.call('wm',
					'iconphoto',
					root._w,
	tk.PhotoImage(file=os.path.join(program_directory,
                                                      "resources",
                                                      "view.gif")))
	root.title("the gooey gui")

	dc = data_bus.DataBus()
	for c in args.connect:
		hostname, port = c.split(":")
		dc.Connect((hostname, int(port)))

	gui = gooey(root, dc, args.all)
	#gui.master.title('gooey gui')


	root.mainloop()