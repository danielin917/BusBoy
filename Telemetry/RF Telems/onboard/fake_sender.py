import datetime
import json
import pprint
import time
import Tkinter
import ttk

import data_bus
import wire

class MessageGenerator(object):
    def __init__(self):
        pass

class FakeSenderFrame(object):
    def __init__(self, record, meta, master):
        self.frame = ttk.Frame(master)
        self._valueFrameContainer = ttk.Frame(master)
        self.valueFrame = ttk.Frame(self._valueFrameContainer)

        self.harness = wire.Harness(meta['harness'])

        wiresColumns = ["Enabled", "Value", "Unit"]
        self.wires = ttk.Treeview(self.frame,
                                  columns=wiresColumns, height=len(self.harness))

        for c in wiresColumns:
            self.wires.heading(c, text=c)

        self.wires.pack(expand=True, fill=Tkinter.BOTH)
        self.wires.column("#0", width=150)
        self.wires.column("Enabled", width=30)
        self.wires.column("Value", width=150)
        self.wires.column("Unit", width=100)

        self.record = record
        self.meta = meta

        self.InitValues()
        self.DrawValues()

        self.wires.selection_set(self.wires.identify_row(0))

        self.valueFrame.pack(fill=Tkinter.BOTH, expand=True)
        self._valueFrameContainer.pack(side=Tkinter.BOTTOM, anchor=Tkinter.W, fill=Tkinter.X, expand=True)

        self.frame.pack(side=Tkinter.BOTTOM, anchor=Tkinter.W, fill=Tkinter.BOTH, expand=True)

    def InitValues(self):
        self.wiresVars = dict()
        self.enabledVars = dict()

        for key in self.harness.keys():
            valueVar = Tkinter.StringVar()
            valueVar.set("")
            enabledVar = Tkinter.IntVar()
            if key=="timestamp":
                enabledVar.set(False)
            else:
                enabledVar.set(True)

            self.wiresVars[key] = valueVar
            self.enabledVars[key] = enabledVar
            valueVar.trace('w', self.DrawValues)
            enabledVar.trace('w', self.DrawValues)

    def DrawValues(self, *args, **kwargs):

        self.wires.delete(*self.wires.get_children())

        for key in self.harness.keys():
            self.wires.insert('', 'end', key, text=key, values=(bool(self.enabledVars[key].get()), self.wiresVars[key].get(), self.meta['harness'][key]['typename']))
        self.wires.bind("<<TreeviewSelect>>", self.ConfigureValue)


    def ConfigureValue(self, event):

        self.valueFrame.destroy()
        self.valueFrame = ttk.Frame(self._valueFrameContainer)
        self.valueFrame.pack(fill=Tkinter.BOTH, expand=True)

        selectedAttrib = self.wires.selection()[0]

        ttk.Label(self.valueFrame, text=selectedAttrib).pack(side=Tkinter.LEFT, padx=20)
        ttk.Label(self.valueFrame, text="Enabled").pack(side=Tkinter.LEFT, padx=5)
        chkbtn = ttk.Checkbutton(self.valueFrame, variable=self.enabledVars[selectedAttrib])
        chkbtn.pack(side=Tkinter.LEFT, padx=1)
        chkbtn.selection_clear()
        ttk.Label(self.valueFrame, text="").pack(side=Tkinter.LEFT, padx=20) #dummy
        ttk.Label(self.valueFrame, text="Value").pack(side=Tkinter.LEFT, padx=5)
        ttk.Entry(self.valueFrame, textvariable=self.wiresVars[selectedAttrib]).pack(side=Tkinter.LEFT, padx=10)

        ttk.Button(self.valueFrame, text="Send", command=self.SendFakeData).pack(side=Tkinter.LEFT, padx=5)
    def SendFakeData(self, *args, **kwargs):
        sendHarness = self.harness#wire.Harness(self.meta['harness'])
        field = self.record.field
        i = field.find("\0")
        if "timestamp" in sendHarness.keys():
            sendHarness.timestamp.value = time.time()
        for key in sendHarness:
            if self.enabledVars[key].get() and self.wiresVars[key].get() != "":
                self.harness[key].value = float(self.wiresVars[key].get())

        self.record.SetValue(sendHarness.buf)

class ConsoleUpdater(object):
    def __init__(self, record, text, scroll):
        self.record = record
        self.text = text
        self.scroll = scroll
        self.record.value_callback.append(self.HandleValue)
        self.record.Subscribe()

    def Unregister(self):
        self.record.value_callback.remove(self.HandleValue)
        self.record.Unsubscribe()

    def HandleValue(self, record):
        self.text.config(state=Tkinter.NORMAL)
        self.text.insert("end", "\n%s" % ([hex(ord(x)) for x in record.value]))
        if self.scroll.get()[1] > 0.9999999:
            self.text.see('end')
        self.text.config(state=Tkinter.DISABLED)

class FakeSender(ttk.Frame):
    def __init__(self, data_bus, subscribe_all, root, *args, **kargs):
        ttk.Frame.__init__(self, root, *args, **kargs)

        self.data_bus = data_bus
        self.data_bus.record_callback.append(self.HandleNewRecord)
        self.data_bus.flush_callback.append(self.HandleFlush)

        self.subscribe_all = subscribe_all

        self.root = root

        self.pane = ttk.PanedWindow(self, orient=Tkinter.HORIZONTAL)
        self.pane.pack(expand=True, fill=Tkinter.BOTH)

        self.tree_frame = ttk.Frame(self.pane)
        self.tree_frame.pack(expand=True, fill=Tkinter.Y)

        self.flush_frame = ttk.Frame(self.tree_frame)
        self.flush_frame.pack(expand=False, side=Tkinter.BOTTOM, pady=10)

        self.flush_button = ttk.Button(self.flush_frame, text="Send Flush", command=self.FlushNow)
        self.flush_button.pack(expand=False)

        self.scroll = ttk.Scrollbar(self.tree_frame)
        self.scroll.pack(side=Tkinter.RIGHT, fill=Tkinter.Y)
        columns = ["Rate"]
        self.tree = ttk.Treeview(self.tree_frame,
                                 yscrollcommand=self.scroll.set,
                                 columns=columns,
                                 selectmode="browse")
        for c in columns:
            self.tree.heading(c, text=c)
            self.tree.column(c, width=64)

        self.tree.pack(expand=True, fill=Tkinter.BOTH, side=Tkinter.LEFT)
        self.scroll.config(command=self.tree.yview)

        self.pane.add(self.tree_frame, weight=0)

        self.data_pane = ttk.PanedWindow(self.pane, orient=Tkinter.VERTICAL)
        self.data_pane.pack(expand=True, fill=Tkinter.BOTH)
        self.pane.add(self.data_pane, weight=1)

        self.data_frames = []
        self.updater = None

        self.HandleFlush()

        self.tree.bind("<<TreeviewSelect>>", self.TreeClick)

        self.Poll()

    def FlushNow(self):
        self.data_bus.Flush()


    def ClearDataPane(self):
        if self.updater:
            self.updater.Unregister()
            self.updater = None
        for frame in self.data_frames:
            self.data_pane.forget(frame)
            frame.destroy()
        self.data_frames = []

    def AddDataFrame(self, scroll=True, expand=True, fill=Tkinter.BOTH, weight=0):
        widget_frame = ttk.Frame(self.data_pane)
        widget_frame.pack(expand=expand, fill=fill)
        self.data_frames.append(widget_frame)
        self.data_pane.add(widget_frame, weight=weight)
        if scroll:
            widget_scroll = ttk.Scrollbar(widget_frame)
            widget_scroll.pack(side=Tkinter.RIGHT, fill=Tkinter.Y)
            return (widget_frame, widget_scroll)
        return widget_frame

    def HandleFlush(self):
        self.ClearDataPane()
        self.records = []
        children = self.tree.get_children("")
        for c in children:
            self.tree.delete(c)

        pass

    def HandleNewRecord(self, record):
        record.tree_key = '%i' % len(self.records)
        self.records.append(record)
        fields = record.field.split('\0')
        names = fields[0].split('.')

        last_parent_path = ""
        for i in range(len(names)):
            parent = names[i]
            parent_path = '.'.join(names[0:i+1])
            if not self.tree.exists(parent_path):
                self.tree.insert(last_parent_path, 'end', parent_path, text=parent)
            last_parent_path = parent_path

        self.tree.insert(last_parent_path, 'end', record.tree_key,
                         text=record.tree_key)

        record.last_rate_timestamp = time.time()
        record.last_rate_count = 0
        record.value_callback.append(self.ComputeRate)

        if self.subscribe_all:
            record.Subscribe()

    def ComputeRate(self, record):
        record.last_rate_count += 1
        now = time.time()
        period = now - record.last_rate_timestamp
        if period >= 1.0:
            rate = record.last_rate_count / period
            record.last_rate_timestamp = now
            record.last_rate_count = 0
            self.tree.set(record.tree_key, "Rate", "%.2f" % rate)

    def TreeClick(self, event):
        items = self.tree.selection()
        if len(items) == 1:
            self.ClearDataPane()
            children = self.tree.get_children(items[0])
            if len(children) == 0:
                record = self.records[int(items[0])]
                fields = record.field.split("\0")
                meta = None
                if len(fields) > 1:
                    meta = json.loads(fields[1])
                    frame, scroll = self.AddDataFrame(weight=1)
                    metadata = Tkinter.Text(frame, yscrollcommand=scroll.set, height=6)
                    metadata.pack(expand=True, fill=Tkinter.BOTH)
                    scroll.config(command=metadata.yview)
                    metadata.insert('end',
                                    json.dumps(meta,
                                               sort_keys=True,
                                               indent=4, separators=(',', ': ')))
                    metadata.config(state=Tkinter.DISABLED)

                if meta and 'harness' in meta:
                    harness = wire.Harness(meta['harness'])

                    controlFrame = self.AddDataFrame(scroll=False, expand=True, weight=1)
                    self.valueFrame = FakeSenderFrame(record, meta, controlFrame)
                else:
                    frame, scroll = self.AddDataFrame(weight=1)
                    data = Tkinter.Text(frame, yscrollcommand=scroll.set, height=6)
                    data.pack(expand=True, fill=Tkinter.BOTH)
                    scroll.config(command=data.yview)
                    data.config(state=Tkinter.DISABLED)
                    self.updater = ConsoleUpdater(record, data, scroll)
                    if record.value:
                        self.updater.HandleValue(record)


    def Poll(self):
        self.data_bus.Poll(time.time(), [],[],[],[])
        self.root.after(20, self.Poll)

if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='Data Bus viewer.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-a', '--all', type=bool, default=True,
                        help='Subscribe to all values.')
    args = parser.parse_args()

    root = Tkinter.Tk()
    root.geometry("800x600")
    root.title("Fake Sender")
    program_directory=sys.path[0]
    root.tk.call('wm',
                 'iconphoto',
                 root._w,
                 Tkinter.PhotoImage(file=os.path.join(program_directory,
                                                      "resources",
                                                      "view.gif")))
    root.title("Fake Sender")

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    fake = FakeSender(dc, args.all, root)
    fake.pack(expand=True, fill=Tkinter.BOTH)

    root.mainloop()
