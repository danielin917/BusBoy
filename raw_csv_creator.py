import csv
import sys

if len(sys.argv) != 3:
	print("Incorrect number of arguments. args:[Input File][Output File]")
	sys.exit()

with open(sys.argv[1]) as f_handle:
	data_lines = f_handle.readlines()

for line in data_lines:
	print(line)
