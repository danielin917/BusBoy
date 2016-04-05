import datetime
import json
import pprint
import time
import Tkinter
import ttk

import data_bus
import wire

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

class TreeUpdater(object):
    def __init__(self, record, harness, tree, mux=None):
        self.record = record
        self.harness = harness
        self.tree = tree
        self.mux = mux
        self.record.value_callback.append(self.HandleValue)
        self.record.Subscribe()

    def Unregister(self):
        self.record.value_callback.remove(self.HandleValue)
        self.record.Subscribe(False)

    def HandleValue(self, record):
        self.harness.buf = buffer(record.value)
        prefix = ""
        if self.mux:
            prefix = str(self.harness[self.mux])
            if not self.tree.exists(prefix):
                self.tree.insert('', 'end', prefix, text=prefix)
        for key in self.harness:
            h = self.harness[key]
            prefixed_key = prefix + "." + key
            if not self.tree.exists(prefixed_key):
                self.tree.insert(prefix, 'end', prefixed_key, text=key,
                                 values=["", h.unit, h.description])
            self.tree.set(prefixed_key, "Value", str(h))


class Viewer(ttk.Frame):
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
                frame = self.AddDataFrame(scroll=False, expand=False)
                title = Tkinter.Label(frame, text=fields[0])
                title.pack(expand=True, fill=Tkinter.BOTH)
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
                    frame, scroll = self.AddDataFrame(weight=1)
                    columns = ["Value", "Unit", "Description"]
                    data = ttk.Treeview(frame, yscrollcommand=scroll.set,
                                        columns=columns, height=len(harness))
                    data.pack(expand=True, fill=Tkinter.BOTH)
                    for c in columns:
                        data.heading(c, text=c)
                    data.column("#0", width=150)
                    data.column("Value", width=150)
                    data.column("Unit", width=100)
                    scroll.config(command=data.yview)

                    if "mux" in meta:
                        self.updater = TreeUpdater(record, harness, data, meta["mux"])
                    else:
                        self.updater = TreeUpdater(record, harness, data)

                    if record.value:
                        self.updater.HandleValue(record)
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
    root.title("Data Viewer")
    program_directory=sys.path[0]
    root.tk.call('wm',
                 'iconphoto',
                 root._w,
                 Tkinter.PhotoImage(file=os.path.join(program_directory,
                                                      "resources",
                                                      "view.gif")))
    root.title("Viewer")

    dc = data_bus.DataBus()
    for c in args.connect:
        hostname, port = c.split(":")
        dc.Connect((hostname, int(port)))

    viewer = Viewer(dc, args.all, root)
    viewer.pack(expand=True, fill=Tkinter.BOTH)

    root.mainloop()
