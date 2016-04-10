//
//  telemetry.cpp
//  Telemetry Class
//
//  Created by Benjamin Katz on 3/14/14.
//  Copyright (c) 2014 Benjamin Katz. All rights reserved.
//


/*
 Put file opening in its own function in fileInfo
 Make the fileInfo constructor only have directory and name info
 copy that over with copy constructor
 then open file stream once it is already in the vector
 */
#include "telemetry.h"




Telemetry::Telemetry(string in_directory, string out_file, int timeInt) {
    numTypes = 0;
    interval = timeInt;
    currentTime = -1;
    lastTime = -1;
    outFile.open(out_file);
    if ( !outFile.is_open() ) {
        cout << "\nERROR\n";
        cerr << "failed to open " << out_file << endl;
        exit(1);
    }
    innitialize(in_directory.c_str());
    times.resize(files.size(), -1);
    outFile << "time,";
    for (int i = 0; i < files.size(); i++) {//prints out the columns
        files[i].openFile();
        files[i].nextLine();
    }
    outFile << endl;
    while (cont()) {
        addLine();
    }
    
}

void Telemetry::innitialize(string in_directory) {
    DIR *dp;
    struct dirent *ep;
    dp = opendir (in_directory.c_str());
    if (dp != NULL) {
        while (ep = readdir (dp)) {
            string insertName = ep->d_name;
            string csv;
			if (insertName.size() >= 3){
            for (int i = 0; i < 3; i++) {
                csv += insertName[insertName.size() - 3 + i];
            }
            ////move this back to fileInfo, dynamically make ifstream
            if (csv == "csv"){
                FileInfo file(ep->d_name, in_directory, outFile, interval);
                numTypes += file.numTypes();
                files.push_back(file);
            }
			}
        }
        (void) closedir (dp);
        //outFile.close();
        //checks that time is formatted correctly
    }
    else {
        cout << "\nERROR\n";
        perror ("Couldn't open the directory");
    }

}

void Telemetry::addLine() {
    for (int i = 0; i < files.size(); i++) {
        times[i] = files[i].nextTime(); //find next time of each file
    }
    int low = -1;
    int i = 0;
    while (i < times.size() && low < 0) {
        if (times[i] > 0) {
            low = i;
        }
        i++;
    }
    assert(low != -1);
    for (int i = low; i < times.size(); i++) {//determine index of lowest time
        if (times[low] > -1 && times[low] > times[i] && times[i] > 0) {
            low = i;
        }
    }
    if (times[low] > lastTime + interval && lastTime > -1) {
        currentTime = lastTime + interval;
    }
    else {
        currentTime = times[low];
    }
    outFile << currentTime << ",";
    for (int i = 0; i < files.size(); i++) {
        files[i].printValues(currentTime);
    }
    outFile << endl;
    lastTime = currentTime;
}


bool Telemetry::cont() {
    for (int i = 0; i < files.size(); i++) {
        if (files[i].fileOpen()) {
            return true;
        }
    }
    return false;
}