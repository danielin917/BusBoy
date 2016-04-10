# For Data Analysis
import pandas

# For Progress Bar in For Loops
from tqdm import tqdm

# For Label Encoding of Features
from sklearn.preprocessing import LabelEncoder

# For scaling
from sklearn.preprocessing import MinMaxScaler

# For Regex
import re

# For parsing XML as ElementTree
import xml.etree.ElementTree as ET

# For converting between Binary And ASCII
import binascii

# For interpreting Strings as Packed Binary Data
from struct import *

# For saving and loading pandas dataframe
import pickle

def get_label_encoded_data(filename, scale=(-1.0, 1.0)):
    dataframes = []
    with open(filename, 'r') as f:
        while True:
            # Get List of Lines in File
            lines = f.readlines()

            # No more lines to read.
            if not lines:
                break

            # Go through the lines with a progress bar (tqdm)
            for line in tqdm(lines):

                # Remove Newlines and replace commas followed by spaces
                # with just a comma
                line = line.replace("\n", "")
                line = line.replace(", ", ",")

                # Get a list using "," as the separator
                arr = line.split(",")
                row = {}

                # For every element of the list except the last
                # (which has multiple comma separated keys and values)
                # create a dictionary with "=" as the separator (key=value).
                for i in range(len(arr) - 1):
                    row[arr[i].split("=")[0]] = arr[i].split("=")[1]

                # The last element of the list above consists itself
                # of keys and values separated by "=". Add them to the
                # the dicitionary.
                for i in range(len(arr[len(arr) - 1].split(" "))):
                    row[arr[len(arr) - 1].split(" ")[i].split("=")[0]] = arr[len(arr) - 1].split(" ")[i].split("=")[1]

                # Append the dictionary to the dataframes list
                dataframes.append(row)

    # Create one large dataframe from the list of row dataframes.
    df = pandas.DataFrame(dataframes)

    # Convert to string for the LabelEncoder.
    df = df.applymap(str)

    # Use the LabelEncoder to encode the Features and MinMaxScaler to
    # scale to [-1.0, 1.0]
    df = df.apply(LabelEncoder().fit_transform)
    df = pandas.DataFrame(MinMaxScaler(feature_range=scale).
                          fit_transform(df))

    print("Completed Parsing Data in: %s!" % filename)
    return df

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

def get_raw_data(dataFilename, XMLStructureFilename):

    with open(dataFilename) as f_handle:
        data_lines = f_handle.readlines()

    dataframes = []

    tree = ET.parse(XMLStructureFilename)
    root = tree.getroot()
    for line in tqdm(data_lines):
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
                data = b""
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
                        processed_val = int(processed_val)
                        if length <= 1:
                            data += pack('B', processed_val)
                        elif length == 2:
                            data += pack('H', int(processed_val))
                        else:
                            data += pack('I', processed_val)
                    elif chunk.tag == 'int' :
                        processed_val = float(processed_val)
                        processed_val -= offset
                        processed_val /= multiplier
                        processed_val = int(processed_val)
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
                    dataframes.append(pandas.DataFrame([unix_ts, binascii.hexlify(data)]))
    return pandas.DataFrame(dataframes)

def save_parsed_data_to_file(dataframe, filename):
    dataframe.to_pickle(filename)

def load_parsed_data_from_file(filename):
    return pandas.read_pickle(filename)