//
//  main.cpp
//  Telemetry 2.0
//
//

#include <iostream>
#include "Telemetry.h"
#include <fstream>
#ifdef _WIN_32
#include <Windows.h>
#endif

int main(int argc, const char * argv[])
{
    string directory = "in_files";

	Telemetry telemetry(directory, "outfile.csv", 1000);
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
	system("pause");
}
