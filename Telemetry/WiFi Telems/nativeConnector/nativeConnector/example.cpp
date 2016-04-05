/**
	Example showing how to connect to and read from the Vehicle Data Center.
**/
#include <iostream>															//cout
#include "Telemetry.h"
using namespace std;
void reader(Telemetric& metric, void* unused)								//this function will be called every time we receive a telemetric
{
	cout<<"trq: "<<metric["trq"]<<endl;
	cout<<"vel: "<<metric["vel"]<<endl;
}
int main()
{
	Telemeter t(string("../vdcmon/dist/vdcmon.jar"));						//instantiate the telemeter with the path to the VDCMon jar.
	string metric="motcmd";													//the metric we want to watch
	vector<string> keys; keys.push_back("trq"); keys.push_back("vel");		//the keys we want to watch
	t.callback(metric,keys,reader);											//register reader() as a callback function for the given metric and keys

    while(true){Sleep(1000000);}											//prevent the main thread from exiting
    return 0;
}
