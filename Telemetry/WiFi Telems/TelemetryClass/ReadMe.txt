Description of Telemetry Class
—————————————————————
Telemetry class:
	 Takes the absolute or relative path of a directory (in_directory), the absolute or relative path of an output file (out_file) and 	
	a desired time interval between data values in milliseconds (timeInterval).

It must include:
	 Fileinfo.h and Telemetry.h 

Other important info:
	Developed on mac, but compatible with Windows.  Requires c++ 11

It is called by:
	Telemetry telem(string in_directory, string out_file, int timeInterval);
	Executable located in TelemetryClass/TelemetryClass


What it does:
	It goes through every csv file in a directory and puts it into a single csv file in the out file 	
	specified.  The output csv file will contain values for every variable within the csv files in the 	
	directory in the correct time slot.  Time slots are created by starting at the earliest time in any of 	
	the csv files and then incrementing by timeInterval until the final time is reached.  Each 	
	variable will be listed as filename.variable (i.e. if file is a.csv and variable is gain, it will be listed 	
	as a.gain).  If more than one value for a given variable falls within the same time slot its overall 	
	value for the given time slot is averaged.  If a variable has values for times preceding and 	
	proceeding a time slot, the value for that time slot is found by extrapolating from that 	information.

Common Errors:
	Error messages are printed to cout.  Most common error is removing the top line of a csv file 	
	within the directory which lists the variable types. 

Important Things to Note:
	-Program only reads csv files, and ignores all others
	-Program assumes first column is a string of the time and date, the second column is the time 	
		 in milliseconds
	-Other than first column and first row, all values must be doubles.  If there are letters the code 	 
	will not output 0’s for that column, and won’t fail, so this is a very important assumption
	-The times of all csv files are  in ascending order, if not an error message will be produced and 	 
		data collected from that particular file should be considered corrupt, but data collected from 	
		others will still work
	-The number of variables must equal the number of commas in every row of the csv files.  if 	
		 not an error message will be produced and data collected from that particular file should 	 
		be considered corrupt, but data collected from others will still work

Unit Testing:
	To conduct unit testing compile unitTest.cpp with Test_docs, outfile.csv and outfile2.csv within 	
	the same directory.  Test passed if there is a cout statement saying test passed, and fails if it 	
	says test fails
	
	The testing files:
	-Combination of file a, b, c test ability to average values, extrapolate values, to 		
		not add values before its first time, and to stop adding values after its last 		
		time, and that values go in the proper time slots

	-File d tests codes ability to handle duplicate time stamps

	-File e tests for when times are not in ascending order

	-File F tests when time is not first value

	-File G tests when ms is not second value

	-File H tests when there is wrong amount of values present

	-File i tests when file is not a csv file
	