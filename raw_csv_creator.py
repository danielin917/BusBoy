import csv
import sys
import re
import xml.etree.ElementTree as ET
import ctypes
import struct
import binascii
from struct import *
def main():
	if len(sys.argv) != 4:
		print("Incorrect number of arguments. args:[Input File][Output File][XML structure file]")
		sys.exit()

	with open(sys.argv[1]) as f_handle:
		data_lines = f_handle.readlines()

	tree = ET.parse(sys.argv[3])
	root = tree.getroot()
	with open(sys.argv[2], 'w+') as csv_handle:
		csv_writer = csv.writer(csv_handle, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
		for line in data_lines:
			data_array = line.split(",")
			#Extract unix time stamp
			time_array = data_array[0].split()
			unix_ts = time_array[len(time_array) - 1]
			#Grab ID
			id_array = data_array[1].split()
			cmd_id = id_array[len(id_array) - 1]
			#Use ID to find xml node
			for child in root.findall('canmsg'):
				if child.get('id') == cmd_id:
					data = ''
					for chunk in child:
						multiplier = float(chunk.get('mult'))
						offset = float(chunk.get('off'))
						length = int(chunk.get('length'))/8
						processed_val = find_val(data_array[2], chunk.get('name'))
						if chunk.tag == 'float':
							processed_val = float(processed_val)	
							processed_val -= offset
							processed_val /= multiplier
							data += pack('f', processed_val)
						elif chunk.tag == 'uint':
							processed_val = float(processed_val)	
							processed_val -= offset
							processed_val /= multiplier
							if length <= 1:
								data += pack('B', processed_val)
							elif length == 2:	
								data += pack('H', processed_val)
							else:	
								data += pack('I', processed_val)
						elif chunk.tag == 'int' :
							processed_val = float(processed_val)
							processed_val -= offset
							processed_val /= multiplier
							if length <= 1:
								data += pack('b', processed_val)
							elif length == 2:
								data += pack('h', processed_val)
							else:
								data += pack('i', processed_val)
							
						elif chunk.tag == 'char':
							processed_val = chr(processed_val)
							processed_val -= offset
							processed_val /= multiplier
							data += pack('c', processed_val)
					csv_writer.writerow([unix_ts, binascii.hexlify(data)])
				
					

def find_val(cmd_string, cmd):
	cmd_array = cmd_string.split()
	for value in cmd_array:
		if value.find(cmd) != -1:
			val = re.search('".*?"', value)
			val  = val.group(0)
			val = val.strip('"')# can be done with RE but I don't feel like looking into it right now	
			return val
	print('failure')
	return 0


if __name__ =='__main__':
	main()
