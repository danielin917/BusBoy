import datetime
import json
import wire

try:
    import pygame
except:
    pygame = None
import time
import Tkinter
import ttk

import data_bus

colors = [
    'snow',
    'ghost white',
    'white smoke',
    'gainsboro',
    'floral white',
    'old lace',
    'linen',
    'antique white',
    'papaya whip',
    'blanched almond',
    'bisque',
    'peach puff',
    'navajo white',
    'lemon chiffon',
    'mint cream',
    'azure',
    'alice blue',
    'lavender',
    'lavender blush',
    'misty rose',
    'dark slate gray',
    'dim gray',
    'slate gray',
    'gray',
    'midnight blue',
    'navy',
    'cornflower blue',
    'dark slate blue',
    'slate blue',
    'medium slate blue',
    'medium blue',
    'royal blue',
    'blue',
    'dodger blue',
    'deep sky blue',
    'sky blue',
    'steel blue',
    'powder blue',
    'pale turquoise',
    'dark turquoise',
    'medium turquoise',
    'turquoise',
    'cyan',
    'cadet blue',
    'medium aquamarine',
    'aquamarine',
    'dark green',
    'dark olive green',
    'dark sea green',
    'sea green',
    'medium sea green',
    'pale green',
    'spring green',
    'lawn green',
    'medium spring green',
    'green yellow',
    'lime green',
    'yellow green',
    'forest green',
    'olive drab',
    'dark khaki',
    'khaki',
    'pale goldenrod',
    'yellow',
    'gold',
    'goldenrod',
    'dark goldenrod',
    'rosy brown',
    'indian red',
    'saddle brown',
    'sandy brown',
    'dark salmon',
    'salmon',
    'orange',
    'dark orange',
    'coral',
    'tomato',
    'orange red',
    'red',
    'hot pink',
    'deep pink',
    'pink',
    'pale violet red',
    'maroon',
    'medium violet red',
    'violet red',
    'medium orchid',
    'dark orchid',
    'dark violet',
    'blue violet',
    'purple',
    'medium purple',
    'thistle',
    'snow2',
    'snow3',
    'snow4',
    'seashell2',
    'seashell3',
    'seashell4',
    'AntiqueWhite1',
    'AntiqueWhite2',
    'AntiqueWhite3',
    'AntiqueWhite4',
    'bisque2',
    'bisque3',
    'bisque4',
    'PeachPuff2',
    'PeachPuff3',
    'PeachPuff4',
    'NavajoWhite2',
    'NavajoWhite3',
    'NavajoWhite4',
    'LemonChiffon2',
    'LemonChiffon3',
    'LemonChiffon4',
    'cornsilk2',
    'cornsilk3',
    'cornsilk4',
    'ivory2',
    'ivory3',
    'ivory4',
    'honeydew2',
    'honeydew3',
    'honeydew4',
    'LavenderBlush2',
    'LavenderBlush3',
    'LavenderBlush4',
    'MistyRose2',
    'MistyRose3',
    'MistyRose4',
    'azure2',
    'azure3',
    'azure4',
    'SlateBlue1',
    'SlateBlue2',
    'SlateBlue3',
    'SlateBlue4',
    'RoyalBlue1',
    'RoyalBlue2',
    'RoyalBlue3',
    'RoyalBlue4',
    'blue2',
    'blue4',
    'DodgerBlue2',
    'DodgerBlue3',
    'DodgerBlue4',
    'SteelBlue1',
    'SteelBlue2',
    'SteelBlue3',
    'SteelBlue4',
    'DeepSkyBlue2',
    'DeepSkyBlue3',
    'DeepSkyBlue4',
    'SkyBlue1',
    'SkyBlue2',
    'SkyBlue3',
    'SkyBlue4',
    'SlateGray1',
    'SlateGray2',
    'SlateGray3',
    'SlateGray4',
    'PaleTurquoise1',
    'PaleTurquoise2',
    'PaleTurquoise3',
    'PaleTurquoise4',
    'CadetBlue1',
    'CadetBlue2',
    'CadetBlue3',
    'CadetBlue4',
    'turquoise1',
    'turquoise2',
    'turquoise3',
    'turquoise4',
    'cyan2',
    'cyan3',
    'cyan4',
    'DarkSlateGray1',
    'DarkSlateGray2',
    'DarkSlateGray3',
    'DarkSlateGray4',
    'aquamarine2',
    'aquamarine4',
    'DarkSeaGreen1',
    'DarkSeaGreen2',
    'DarkSeaGreen3',
    'DarkSeaGreen4',
    'SeaGreen1',
    'SeaGreen2',
    'SeaGreen3',
    'PaleGreen1',
    'PaleGreen2',
    'PaleGreen3',
    'PaleGreen4',
    'SpringGreen2',
    'SpringGreen3',
    'SpringGreen4',
    'green2',
    'green3',
    'green4',
    'chartreuse2',
    'chartreuse3',
    'chartreuse4',
    'OliveDrab1',
    'OliveDrab2',
    'OliveDrab4',
    'DarkOliveGreen1',
    'DarkOliveGreen2',
    'DarkOliveGreen3',
    'DarkOliveGreen4',
    'khaki1',
    'khaki2',
    'khaki3',
    'khaki4',
    'yellow2',
    'yellow3',
    'yellow4',
    'gold2',
    'gold3',
    'gold4',
    'goldenrod1',
    'goldenrod2',
    'goldenrod3',
    'goldenrod4',
    'DarkGoldenrod1',
    'DarkGoldenrod2',
    'DarkGoldenrod3',
    'DarkGoldenrod4',
    'RosyBrown1',
    'RosyBrown2',
    'RosyBrown3',
    'RosyBrown4',
    'IndianRed1',
    'IndianRed2',
    'IndianRed3',
    'IndianRed4',
    'sienna1',
    'sienna2',
    'sienna3',
    'sienna4',
    'burlywood1',
    'burlywood2',
    'burlywood3',
    'burlywood4',
    'wheat1',
    'wheat2',
    'wheat3',
    'wheat4',
    'tan1',
    'tan2',
    'tan4',
    'chocolate1',
    'chocolate2',
    'chocolate3',
    'firebrick1',
    'firebrick2',
    'firebrick3',
    'firebrick4',
    'brown1',
    'brown2',
    'brown3',
    'brown4',
    'salmon1',
    'salmon2',
    'salmon3',
    'salmon4',
    'orange2',
    'orange3',
    'orange4',
    'DarkOrange1',
    'DarkOrange2',
    'DarkOrange3',
    'DarkOrange4',
    'coral1',
    'coral2',
    'coral3',
    'coral4',
    'tomato2',
    'tomato3',
    'tomato4',
    'OrangeRed2',
    'OrangeRed3',
    'OrangeRed4',
    'red2',
    'red3',
    'red4',
    'DeepPink2',
    'DeepPink3',
    'DeepPink4',
    'HotPink1',
    'HotPink2',
    'HotPink3',
    'HotPink4',
    'pink1',
    'pink2',
    'pink3',
    'pink4',
    'PaleVioletRed1',
    'PaleVioletRed2',
    'PaleVioletRed3',
    'PaleVioletRed4',
    'maroon1',
    'maroon2',
    'maroon3',
    'maroon4',
    'VioletRed1',
    'VioletRed2',
    'VioletRed3',
    'VioletRed4',
    'magenta2',
    'magenta3',
    'magenta4',
    'orchid1',
    'orchid2',
    'orchid3',
    'orchid4',
    'plum1',
    'plum2',
    'plum3',
    'plum4',
    'MediumOrchid1',
    'MediumOrchid2',
    'MediumOrchid3',
    'MediumOrchid4',
    'DarkOrchid1',
    'DarkOrchid2',
    'DarkOrchid3',
    'DarkOrchid4',
    'purple1',
    'purple2',
    'purple3',
    'purple4',
    'MediumPurple1',
    'MediumPurple2',
    'MediumPurple3',
    'MediumPurple4',
    'thistle1',
    'thistle2',
    'thistle3',
    'thistle4',
    'gray1']


class TextPlus(Tkinter.Text):
    def __init__(self, *args, **kwargs):
        Tkinter.Text.__init__(self, *args, **kwargs)
        _rc_menu_install(self)
        self.bind("<Button-3><ButtonRelease-3>", self.show_menu)

    def show_menu(self, e):
        self.tk.call("tk_popup", self.menu, e.x_root, e.y_root)

def _rc_menu_install(w):
    w.menu = Tkinter.Menu(w, tearoff=0)
    w.menu.add_command(label="Copy")

    w.menu.entryconfigure("Copy", command=lambda: w.focus_force() or w.event_generate("<<Copy>>"))



class FakeSender(ttk.Frame):
    def __init__(self, name, data_bus, root, *args, **kargs):
        ttk.Frame.__init__(self, root, *args, **kargs)

        self.name = name
        self.data_bus = data_bus
        self.data_bus.record_callback.append(self.HandleNewRecord)
        self.data_bus.flush_callback.append(self.HandleFlush)

        self.root = root

        self.chat_frame = ttk.Frame(self)
        self.chat_frame.pack(expand=True, fill=Tkinter.BOTH)

        self.scroll = ttk.Scrollbar(self.chat_frame)
        self.scroll.pack(side=Tkinter.RIGHT, fill=Tkinter.Y)
        self.chat = TextPlus(self.chat_frame, height=6,
                             yscrollcommand=self.scroll.set)
        self.chat.pack(expand=True, fill=Tkinter.BOTH, side=Tkinter.LEFT)
        self.scroll.config(command=self.chat.yview)
        self.chat.config(state=Tkinter.DISABLED)

        self.entry_frame = ttk.Frame(self)
        self.entry_frame.pack(fill=Tkinter.X)

        self.entry = Tkinter.Text(self.entry_frame, height=3, width=32)
        self.entry.pack(expand=True, fill=Tkinter.BOTH, side=Tkinter.LEFT)
        self.entry.bind("<Shift-Return>", self.InsertNewline)
        self.entry.bind("<Return>", self.Send)

        self.send = ttk.Button(self.entry_frame, text="Send", command=self.Send)
        self.send.pack(side=Tkinter.RIGHT, fill=Tkinter.Y)

        if pygame is not None:
            pygame.mixer.init()
            self.sound = pygame.mixer.Sound(os.path.join("resources", "chat.wav"))

        self.HandleFlush()

        self.Poll()

    def InsertNewline(self, event):
        self.entry.insert(Tkinter.INSERT, "\n")
        return "break"

    def Send(self, event = None, msg=None):
        if(msg is None):
            msg = self.entry.get("1.0", "end - 1 char")
        if msg:
            metadata = {"name": self.name,
                        "timestamp":time.time(),
                        "message":msg}
            field = "chat.message\0%s" % (json.dumps(metadata))
            self.entry.delete("1.0", "end")
            self.data_bus.AddRecord(field)

        if event:
            return "break"
    def SendRaw(self, name, data):
        field = "%s\0%s" % (name, json.dumps(data))
        self.entry.delete("1.0", "end")
        self.data_bus.AddRecord(field)



    def HandleFlush(self):
        self.chat.config(state=Tkinter.NORMAL)
        self.chat.delete("1.0", "end")
        self.messages = [(2**64-1, "endmark")]
        end_mark = "%i_%s" % self.messages[0]
        self.chat.mark_set(end_mark, "1.0")
        self.chat.config(state=Tkinter.DISABLED)

    def FindIndex(self, start, end, timestamp):
        if start + 1 >= end:
            if self.messages[start][0] > timestamp:
                return start
            else:
                return end

        mid = (start + end) / 2
        if timestamp < self.messages[mid][0]:
            return self.FindIndex(start, mid, timestamp)
        return self.FindIndex(mid, end, timestamp)

    def HandleNewRecord(self, record):
        field = record.field
        i = field.find("\0")
        metadata = json.loads(field[i+1:])
        self._harness = wire.Harness(metadata["harness"])
        for key in self._harness:
            self._harness[key].value = 123
        self._harness.timestamp.value = time.time()

        record.SetValue(self._harness.buf)

        if field.startswith("chat.message"):
            name = metadata["name"]
            timestamp = metadata["timestamp"]
            message = metadata["message"]

            i = self.FindIndex(0, len(self.messages), timestamp);
            self.messages.insert(i, (timestamp, name))

            self.chat.config(state=Tkinter.NORMAL)
            end_mark = "%i_%s" % self.messages[i+1]
            mark = "%i_%s" % (timestamp, name)
            self.chat.mark_set(mark, end_mark)
            self.chat.mark_gravity(mark, Tkinter.LEFT)

            timestring = datetime.datetime.fromtimestamp(timestamp).strftime(
                '%Y-%m-%d %H:%M:%S')
            self.chat.mark_set(Tkinter.INSERT, end_mark)
            self.chat.insert(Tkinter.INSERT, "%s " % (timestring))
            self.chat.insert(Tkinter.INSERT, "%s> " % (name), (name))
            self.chat.insert(Tkinter.INSERT, "%s\n" % (message))
            self.chat.mark_gravity(mark, Tkinter.RIGHT)

            color_hash = hash(name) % len(colors)
            self.chat.tag_config(name, foreground=colors[color_hash])
            if self.scroll.get()[1] > 0.9999999:
                self.chat.see('end')
            self.chat.config(state=Tkinter.DISABLED)

            if self.data_bus.bootstrapped and name != self.name:
                if pygame is not None:
                    self.sound.play()


    def Poll(self):
        self.data_bus.Poll(time.time(), [],[],[],[])
        self.root.after(200, self.Poll)

    def Sandwich(self):
        metadata = {
            "timestamp": time.time(),
            "trq": 12345.678,
            "vel": 98765.432,
            }
        self.SendRaw("motcmd", metadata)
        #self.root.after(200, self.Sandwich)

if __name__ == "__main__":
    import argparse
    import os
    import sys
    import tkSimpleDialog

    parser = argparse.ArgumentParser(description='Data Bus chat client.')
    parser.add_argument('connect', metavar='hostname:base_port',
                        type=str, nargs='*',
                        help='Data Buses to connect to.')
    parser.add_argument('-n', '--name', type=str,
                        help='Screen name to use')
    args = parser.parse_args()

    root = Tkinter.Tk()
    root.geometry("640x480")
    root.title("Fake Telemetry Sender")
    program_directory=sys.path[0]
    root.tk.call('wm',
                 'iconphoto',
                 root._w,
                 Tkinter.PhotoImage(file=os.path.join(program_directory,
                                                      "resources",
                                                      "chat.gif")))

    home_dir = os.path.expanduser("~")
    config = os.path.join(home_dir, "dc_chat_name.txt")
    if args.name:
        name = args.name
        with open(config, "w") as f:
            f.write(name)
    else:
        try:
            with open(config, "r") as f:
                name = f.read().strip()
        except IOError:
            name = tkSimpleDialog.askstring("Screenname", "What is your name?")
            if not name:
                print "No screen name provided."
                sys.exit(-1)
            with open(config, "w") as f:
                f.write(name)

    root.title("Fake Telemetry Sender")

    dc = data_bus.DataBus()
    dc.Connect(("127.0.0.1", int(10000)))

    fakeSender = FakeSender(name, dc, root)
    fakeSender.pack(expand=True, fill=Tkinter.BOTH)

    metadata = {
        "timestamp": time.time(),
        "trq": 12345.678,
        "vel": 98765.432,
        }
    #fakeSender.Sandwich()

    root.mainloop()
