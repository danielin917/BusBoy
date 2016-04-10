//
//  telemetry.h
//  Telemetry Class
//
//  Created by Benjamin Katz on 3/14/14.
//  Copyright (c) 2014 Benjamin Katz. All rights reserved.
//

#ifndef __Telemetry_Class__telemetry__
#define __Telemetry_Class__telemetry__

#include "fileInfo.h"
#include <cstring>
#include <iostream>
#include <fstream>
#include <cmath>
#include <vector>
#include <string>
#include <dirent.h>
#include <stdio.h>
#include <sys/types.h>
#include <cstdlib>

using namespace std;

class Telemetry {
public:
    
    //nonDefault constructor
    
    //Requires: the absolute or relative path of a directory for in_directory[], the absolute or relative path for a file (that need not already exist) for out_file, and a positive int for timeInt in miliseconds
    //Modifies: the file created at out_file[]
    //Effects: Goes through all the csv files in the directory in_directory[], and compiles them into a single csv file at out_file[], giving data values at an interval of timeInt
    
    Telemetry(string in_directory, string out_file, int timeInt);
    
    
    
private:
    //opens up all the files in the directory and gives each file a fileInfo object inside of a vector of fileInfo objects, wi
    void innitialize(string in_directory);
    
    //does next line of output
    void addLine();
    
    //checks if any filestreams are still open (whether program should continue)
    bool cont();
    
    ////////////////////////////////////////
    //member variables
    
    //total number of types from all files
    long numTypes;
    
    //time interval in milliseconds
    int interval;
    
    //current time
    long long currentTime;
    
    //keeps track of all times
    vector< long long> times;
    
    //keeps track of previous time used
    long long lastTime;
    
    //keeps track of each file stream and its relavent information
    vector< FileInfo > files;
    
    //keeps track of outFile stream
    ofstream outFile;
    

    
};

#endif  defined(__Telemetry_Class__telemetry__)
