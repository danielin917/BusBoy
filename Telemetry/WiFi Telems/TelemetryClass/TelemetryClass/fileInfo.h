//
//  fileInfo.h
//  Telemetry 2.0
//
//  Created by Benjamin Katz on 4/8/14.
//  Copyright (c) 2014 Benjamin Katz. All rights reserved.
//

#ifndef __Telemetry_2_0__fileInfo__
#define __Telemetry_2_0__fileInfo__

#include <cstring>
#include <iostream>
#include <fstream>
#include <cmath>
#include <vector>
#include <string>
#include <cstring>
#include <dirent.h>
#include <stdio.h>
#include <sys/types.h>
#include <cstdlib>
#include <cassert>

using namespace std;

class FileInfo {
public:
    
    //constructor that takes in file name (in_file) the directory name (path),
    //the output file stream (out_file) and the time interval desired
    //between two values (timeInt)
    FileInfo( string in_file, string path, ofstream &out_file, int timeInt);
    
    //copy constructor for FileInfo
    FileInfo(const FileInfo &other);
    
    //opens the filestream
    void openFile();
        
    //function that reads in nextLine of fileInfo
    void nextLine();
    
    //function that gives the next time for the last read in value
    long long nextTime();
    
    //prints the values for each type to the output file for a given time input
    void printValues(long long time);
    
    //returns true if file stream is still open, and false otherwise
    bool fileOpen();
    
    //returns number of types within the file
    int numTypes();
    
    //prints type names to the output file
    void printTypes();
    
    
private:
    
    
    //keeps track of the name of the file
    string fileName;
    
    //keeps track of the directories path
    string directoryName;
    
    //keeps track of line number for error tracking purposes
    int lineNum;
    
    //keeps track of previously read in and most recently read in lines which are parsed by type
    vector<string> prevVal, curVal;
    
    //keeps track of number of types within a file
    long types;
    
    //keeps track of weather program has reached the end of the filestream
    bool isOpen;
    
    //keeps track of the filestream for the file
    ifstream inFile;
    
    //outFile
    ofstream &outFile;
    
    
    //keeps track of last read in time and currently read in time
    long long currentTime, lastTime;
    
    //Desired time interval between two values
    int interval;
    
    
    
};



#endif /* defined(__Telemetry_2_0__fileInfo__) */
