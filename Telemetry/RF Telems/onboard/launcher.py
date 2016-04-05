import atexit
import json
import os
import Queue
import subprocess
import threading
import Tkinter
import ttk


banned_presses = [
    "BackSpace",
    "space",
    "Return",
    "asciitilde",
    "exclam",
    "at",
    "numbersign",
    "dollar",
    "percent",
    "asciicircum",
    "ampersand",
    "asterisk",
    "parenleft",
    "parenright",
    "underscore",
    "plus",
    "grave",
    "minus",
    "equal",
    "Tab",
    "backslash",
    "bracketright",
    "bracketleft",
    "apostrophe",
    "semicolon",
    "slash",
    "period",
    "comma",
    "bar",
    "braceright",
    "braceleft",
    "quotedbl",
    "colon",
    "question",
    "greater",
    "less"
]


def KeyPress(event):
    if len(event.keysym) <= 1 or event.keysym in banned_presses:
        return "break"

class ReadOnlyText(Tkinter.Text):
    def __init__(self, *args, **kargs):
        Tkinter.Text.__init__(self, *args, **kargs)
        self.bind("<KeyPress>", KeyPress)
        self.bind("<Control-Key-a>", self.SelectAll)

    def SelectAll(self, event):
        self.tag_add("sel", "1.0", "end - 1 char")
        return "break"


class Process(object):
    def __init__(self, cmd, frame):
        self.cmd = cmd
        self.scroll = ttk.Scrollbar(frame)
        self.scroll.pack(side=Tkinter.RIGHT, fill=Tkinter.Y)
        self.console = ReadOnlyText(frame,
                                    yscrollcommand=self.scroll.set)
        self.console.pack(side=Tkinter.LEFT,
                          expand=True, fill=Tkinter.BOTH)
        self.scroll.config(command=self.console.yview)

        self.console.tag_configure("stdout")
        self.console.tag_configure("stdin", background="red")
        self.console.tag_configure("error", foreground="red")

        self.started = False
        self.last_code = None

    def ReadOutput(self, label, stream):
        while True:
            line = stream.readline()
            if line:
                try:
                    self.output.put((label, line), block=False)
                except Queue.Full:
                    print "Overflow:", label, line 
            else:
                break

    def Stop(self):
        if self.IsRunning():
            self.process.terminate()
            self.process.wait()
        self.started = False
        self.process = None

    def Start(self):
        if self.IsRunning():
            return
        self.console.delete("1.0", "end")
        self.output = Queue.Queue(maxsize=10000)
        self.last_code = None
        self.started = True
        self.process = subprocess.Popen(self.cmd, shell=False, bufsize=0,
                                        stdout=subprocess.PIPE,
                                        stderr=subprocess.PIPE)
        self.thread1 = threading.Thread(target=self.ReadOutput,
                                        args=("stdout", self.process.stdout))
        self.thread2 = threading.Thread(target=self.ReadOutput,
                                        args=("stderr", self.process.stderr))
        self.thread1.daemon = True
        self.thread2.daemon = True
        self.thread1.start()
        self.thread2.start()
        self.console.insert("end", " ".join(["Running:"] + self.cmd))

    def IsRunning(self):
        return self.started and self.process.poll() is None

    def Poll(self):
        if not self.started:
            return

        try:
            while True:
                label, text = self.output.get(block=False)
                self.console.insert("end", "\n" + text.rstrip(), (label))
        except Queue.Empty:
            pass

        ret = self.process.poll()
        if ret is not None and self.last_code is None:
            self.last_code = ret
            if ret != 0:
                self.console.insert(
                    "end",
                    "\nProcess terminated with code %i.\n" % (ret), ("error"))
            else:
                self.console.insert("end", "\nProcess completed.\n")

        numlines = int(self.console.index("end - 1 line").split(".")[0]) - 1
        if numlines > 2000:
            self.console.delete("1.0", "%i.0" % (numlines - 1999))
            numlines = 2000

        if self.scroll.get()[1] > 0.9999999:
            self.console.see('end')


class Launcher(ttk.Frame):
    def __init__(self, config, root, *args, **kargs):
        ttk.Frame.__init__(self, root, *args, **kargs)

        self.root = root
        self.pane = ttk.PanedWindow(self, orient=Tkinter.HORIZONTAL)
        self.pane.pack(expand=True, fill=Tkinter.BOTH)

        self.list_frame = ttk.Frame(self.pane)
        self.list_frame.pack(fill=Tkinter.Y)
        self.pane.add(self.list_frame)
        self.list = ttk.Treeview(self.list_frame)
        self.list.pack(expand=True, fill=Tkinter.BOTH)
        self.list.bind("<<TreeviewSelect>>", self.ListClick)
        self.list.bind("<Control-Key-a>", self.SelectAll)
        self.list.bind("<Control-w>", self.Start)
        self.list.bind("<Control-s>", self.Stop)

        self.list.tag_configure(
            "started",
            background="#aaffaa",
            font="{bold}")
        self.list.tag_configure("died", background="#ffaaaa")
        self.list.tag_configure("stopped",)

        self.start = ttk.Button(self.list_frame, text="Start", command=self.Start)
        self.start.pack(side=Tkinter.LEFT, expand=True, fill=Tkinter.X)
        self.stop = ttk.Button(self.list_frame, text="Stop", command=self.Stop)
        self.stop.pack(side=Tkinter.LEFT, expand=True, fill=Tkinter.X)

        self.tabs = ttk.Notebook(self.pane)
        self.tabs.pack(expand=True, fill=Tkinter.BOTH)
        self.pane.add(self.tabs)

        self.config = json.load(open(config, "r"))
        self.processes = {}
        for process in self.config["processes"]:
            frame = ttk.Frame(self.tabs)
            frame.pack(expand=True, fill=Tkinter.BOTH)
            self.processes[process["name"]] = Process(process["cmd"], frame)
            atexit.register(self.processes[process["name"]].Stop)
            self.list.insert('', 'end', process["name"], text=process["name"])
            self.tabs.add(frame, text=process["name"], state="hidden")

        for process in self.config["processes"]:
            if process["autostart"].upper() == "TRUE":
                self.processes[process["name"]].Start()

        self.Poll()

    def Start(self, event = None):
        for item in self.list.selection():
            self.processes[item].Start()

    def Stop(self, event = None):
        for item in self.list.selection():
            self.processes[item].Stop()

    def SelectAll(self, event):
        self.list.selection_set(self.list.get_children())

    def ListClick(self, event):
        items = self.list.selection()
        if len(items) == 1:
            for index in range(self.tabs.index("end")):
                self.tabs.tab(index, state="hidden")
            index = self.list.index(items[0])
            self.tabs.select(index)

    def Poll(self):
        for key, obj in self.processes.items():
            obj.Poll()
            if obj.IsRunning():
                self.list.item(key, tags=("started"))
            elif obj.started and obj.last_code:
                self.list.item(key, tags=("died"))
            else:
                self.list.item(key, tags=("stopped"))

        self.root.after(200, self.Poll)

import sys

root = Tkinter.Tk()
root.title("Launcher")
program_directory=sys.path[0]
root.tk.call('wm', 'iconphoto', root._w,
             Tkinter.PhotoImage(file=os.path.join(program_directory,
                                                  "resources",
                                                  "launch.gif")))
l = Launcher(sys.argv[1], root)
l.pack(expand=True, fill=Tkinter.BOTH)
root.mainloop()
