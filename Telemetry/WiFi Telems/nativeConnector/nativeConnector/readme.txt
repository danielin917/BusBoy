 === Telemetry ===
The classes in the file Telemetry.h provide an interface for
reading data from the Vehicle Data Center in C++.

To connect to the Vehicle Data Center, create an instance of
the Telemeter class. Pass to its constructor or to the open()
function the location of the VDCConn.jar file, and optionally
the java.exe executable. (If you do not provide the latter,
the Telemeter will attempt to find it for you.)

 === Reading Data ===
Data is read from the VDC by registering handlers for particular
telemetrics. These functions will be run asynchronously whenever
a message pertaining to them is received. At present, do not
attempt to register two handlers for the same metric. However,
this feature will be added soon.

To register a handler, call the .callback member function. It
has the following signature:

.callback(string metric,vector<string> keys,telemetricHandler reader, void* PARAM=NULL);
 - metric is the particular telemetric you wish to measure (e.g. motcmd)
 - keys are which keys within this metric you wish to measure. Only
   those keys which are specified here will be reported to your
   handler.
 - reader is a pointer to your handler function, which will
   receive the can messages.
 - PARAM is a void pointer which will always be passed to your
   handler function every time it is run. This is to allow your
   function to communicate with the rest of your program.

Your handler function must have the following signature:

void function(Telemetric&, void*);
 - Your program will receive a particular Telemetric each
   time a CAN message is received. It will also receive
   the void pointer you passed the .callback function at
   initialization time. It is required that your function
   accept this void pointer, but it may choose not to use it.

==The Telemetric Class==
Every time a CAN message is received, a Telemetric is sent
to the handler you registered with the .callback() function.
Telemetrics have the following public member functions:
	string getType();
		-returns the type of the message (typically data)
	string getMetric();
		-returns the metric name (e.g. motcmd)
	string get(string k);
		-returns the value associated with a particular key
	string operator[](string k);
		-same as get(string k)
	const map<string,string>& get();
		-returns the internal key/value map.


An example of use can be found in example.cpp.

 === Compiliation ===
These files depend on files located in the following directories:
 - 14Code/include/cpp (subversion)
 - Tools/include/cpp (git)
If /include/cpp is not already part of your project's include directories,
you'll need to add it. If you're using the provided code::blocks
project, you will be automatically prompted to specify this directory
(cpp_include_directory) the first time you attempt to compile
the code.
