//
//  unitTest.cpp
//  Telemetry 2.0
//
//  Created by Benjamin Katz on 4/19/14.
//  Copyright (c) 2014 Benjamin Katz. All rights reserved.
//

#include <iostream>
#include "Telemetry.h"
#include <fstream>
#ifdef _WIN_32
#include <Windows.h>
#endif

int main(int argc, const char * argv[])
{
    string directory = "test_docs";

	Telemetry telemetry(directory, "outfile.csv", 1000);
    Telemetry telemetry2(directory, "outfile2.csv", 500);
    ifstream test;
    ifstream compare;
    test.open("outfile.csv");
    if (!test.is_open()) {
        cout << "outfile.csv did not open\n";
    }
    compare.open("outfileTest.csv");
    if (!compare.is_open()) {
        cout << "outfileTest.csv did not open\n";
    }
    string testLine;
    string compareLine;
    string temp;
    while (getline(test, temp)) {
        testLine += temp;
    }
    while (getline(compare, temp)) {
        compareLine += temp;
    }
    ifstream test2;
	ifstream compare2;
    test2.open("outfile2.csv");
    if (!test2.is_open()) {
        cout << "outfile2.csv did not open\n";
    }
    compare2.open("outfileTest2.csv");
    if (!compare2.is_open()) {
        cout << "outfileTest2.csv did not open\n";
    }
    string testLine2;
    while (getline(test2, temp)) {
        testLine2 += temp;
    }
    string compareLine2;
    while (getline(compare2, temp)) {
        compareLine2 += temp;
    }
    if (compareLine == testLine && compareLine2 == testLine2) {
        cout << "Test Passed!!\n";
    }
    else {
        cout << "Test Failed\n";
    }
	system("pause");
}
