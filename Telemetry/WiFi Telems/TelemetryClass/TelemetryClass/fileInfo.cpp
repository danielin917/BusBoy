//
//  fileInfo.cpp
//  Telemetry 2.0
//
//  Created by Benjamin Katz on 4/8/14.
//  Copyright (c) 2014 Benjamin Katz. All rights reserved.
//

#include "fileInfo.h"

FileInfo::FileInfo( string in_file, string path, ofstream &out_file, int timeInt)
:   outFile(out_file)
{
    directoryName = path;
    fileName = in_file;
    lineNum = 0; //innitialized to -1 since no lines have yet been read
    interval = timeInt;
    currentTime = lastTime = -1; //innitialized to -1 to show no times have been read yet

}


//copy constructor
FileInfo::FileInfo(const FileInfo &other)
:   outFile( other.outFile){
    directoryName = other.directoryName;
    fileName = other.fileName;
    lineNum = other.lineNum;
    currentTime = other.currentTime;
    lastTime = other.lastTime;
    interval = other.interval;
}

void FileInfo::openFile() {
    string openFile = directoryName + "/" + fileName;
    inFile.open(openFile);
    isOpen = true;
    if ( !inFile.is_open() ) {
        cout << "\nERROR\n";
        cerr << "open failed for " << fileName << endl << endl;
        isOpen = false;
        inFile.close();
        types = 0;
    }
    else {
        nextLine();
        types = curVal.size() - 2;//dont include the date or time in types since they are in every file
        printTypes();
    }

}

long long FileInfo::nextTime() {
    if (isOpen) {
        return currentTime;
    }
    return -1;
}

void FileInfo::nextLine() {
    lineNum++;
    prevVal = curVal;
    string line;
    if (getline(inFile, line)) {//done to see if last line has been reached
        vector<string> temp (1);//vector to temporarily store parsed line data
        int index = 0;
        string end;
        for (long i = line.length() - 2; i < line.length(); i++) {
            end += line[i];
        }
        for (int i = 0; i < line.length(); i++) {//-2 to avoid hidden chars \r
            if (line[i] != ',') {//commas indicate new type
                temp[index] += line[i];
            }
            else {
                index++;
                if (i != line.length()) {
                    temp.push_back("");
                }
            }
        }
        if (temp[temp.size() -1] == "\r" || end != "\r") {//done to prevent extra empty spots in vectors, and to make sure that the last line (which does not end with a new line) also has proper amount of spots
            temp.pop_back();
        }
        curVal = temp;
        if (lineNum > 1) {
            lastTime = currentTime;
            currentTime = floor(atof(curVal[1].c_str())/interval) * interval;//update time
            if (lastTime > currentTime) {
                cout << "\nERROR\n";
                cout << "line " << lineNum << " of " << fileName << " has a smaller time value than previous time\n";
                isOpen = false;
                inFile.close();
            }
            if (curVal.size() != numTypes() + 2) {//checks that number of slots is always equal to number of types listed on first line, if it isnt print error and stop printing values for this file
                //means there is a line with more values than there are types which is an issue!
                cout << "\nERROR\n";
                cout << "line " << lineNum << " of " << fileName << " has incorrect number of values\n";
                isOpen = false;
                inFile.close();
            }
        }
    }
    else {//if getline fails
        isOpen = false;
        inFile.close();
        for (int i = 0; i < curVal.size(); i++) {
            curVal[i] = "";//print null values
        }
    }
}

void FileInfo::printValues(long long time) {
    vector<vector<double> > toPrint (types, vector < double > (2));
    //keeps track of value (in col 0) and repitions (in col 1) for the value to be printed next
    if (isOpen) {
        if (currentTime == time) {
            do {//while time equals current add value to proper row of toPrint col 0, andd increment number of repititions of values in proper row of toPrint col 1.
                //This allows us to figure out either no values are present (in which case nothing is printed out) or average values if a type has multiple values within a given time interval
                for (int i = 0; i < types; i++) {
                    toPrint[i][0] += atof(curVal[i + 2].c_str());
                    toPrint[i][1] ++;
                }
            nextLine();//get next line and see if it is still within the same time interval
            }
            while (currentTime == time && isOpen);
        }
        else if (lastTime == -1) {}//want toPrint to be empty, means its first time value is larger than time
        else if (lastTime < time && currentTime > time) {
            double multiplier = double(time - lastTime) / (currentTime - lastTime);
            for (int i = 0; i < types; i++) { //guesses intermediate values based previous and next time value
                toPrint[i][0] += atof(prevVal[i + 2].c_str()) + multiplier * (atof(curVal[i + 2].c_str()) - atof(prevVal[i + 2].c_str()));
                toPrint[i][1] ++;
            }
        }
        else  {//currentTime is not equal to time, is not the first entry and lastTime is not less than time with currentTime greater than time
            isOpen = false;
            inFile.close();
        }
    }
    if (currentTime == -1 && isOpen) {
        cout << "\n\nWARNING!\n" << fileName << " contains no values\n";
    }
    for (int i = 0; i < types; i++) {
        if (toPrint[i][1] != 0) {//if there is more than one repition within the time slot, insert the average time over time period
            outFile << toPrint[i][0]/toPrint[i][1] << ",";
        }
        else {//if there are none then just print a comma
            outFile << ",";
        }
    }
}

bool FileInfo::fileOpen() {
    return isOpen;
}

int FileInfo::numTypes() {
    return static_cast<int>(types);
}

void FileInfo::printTypes() {
    if (curVal[0] == "time" && curVal[1] == "ms") {//time and ms should be first two values or incorrectly formatted
        cout << fileName << endl;
        string insertName = fileName;
        for (int k = 0; k < 3; k++) {//remove csv from file name
            insertName.pop_back();
        }
        for (int i = 0; i < types; i++) {
            outFile  << insertName << curVal[i + 2] << ",";
        }
    }
    else {
        cout << "\nERROR\n";
        cout << fileName << " is not formatted correctly\ntime and ms are not first two items in the file\n\n";
        bool isOpen = false;
        inFile.close();
		types = 0;
    }
}

