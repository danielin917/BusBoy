The parsing program is in Telemetry/WiFi Telems/VehicleDataCenter

canstructure.xml is the file used by the telemetry java application to
parse raw CAN packets from a WiFi network into the format in the Sample
Data file.

In the canstructure.xml file, each canmsg definition header has two fields,
the 'address' field and the 'id' field. The address field is the CAN address
of the message, the id field is a name given to a message for readability.
The id field directly translates to the id field in the sample data.

The canmsg data has five fields, 'name', 'mult', 'off', 'length', and
'startbit'. The name, mult, and off fields are closely related to each other
as analogs to different parts of the y=mx+b function. Taking x to be the raw
data from the CAN bus, y is the name field, m is the mult field, and off is
b.

The length and startbit fields specify where within the raw data each data
field is located. length is the length of a data type in bits, start bit is
the first bit of the data type.