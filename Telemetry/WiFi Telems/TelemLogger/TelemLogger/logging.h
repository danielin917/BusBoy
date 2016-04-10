#include <INI.h>
#include <solarException.h>
#include <mysql.h>
#define MYSQL_LIB_PATH "../mysql.lib"
#include "../../../Telemetry/nativeConnector/nativeConnector/Telemetry.h"
#include <string>
#include <iomanip>
using namespace std;
inline long long int floorTimestamp(long long int timestamp, long long delay)
{
	long long int temp=timestamp/delay;
	return temp*delay;
}
struct logger_struct
{
	mysql* db;
	long long delay;
	long long lastTime;//last time that was written
	long long* globalLastTime;
	vector<double> lastWrites;
	vector<vector<double> > history;
};
inline void logTelemToDb(Telemetric& metric, void* lg_void)
{
	logger_struct* lg = (logger_struct*) lg_void;

	*lg->globalLastTime=metric.getTimestamp();
	string columns="vdcTime, segId";
	double ts=((double)metric.getTimestamp());
	if(lg->delay!=0) 
		ts=floorTimestamp(ts,lg->delay)/1000.0;
	stringstream temp;
	temp<<fixed<<showpoint<<setprecision(3);
	temp<<"FROM_UNIXTIME("<<ts<<")";
	string values;
	values=temp.str();
	values=values+", "+itos(metric.getSegment());
	const map<string,string>& keys=metric.get();
	//if in realtime mode, write to database immediately
	if(lg->delay==0)
	{
		for(auto it=keys.begin();it!=keys.end();++it)
		{
			columns=columns+", "+it->first;
			values=values+", "+it->second;
		}
		string query="INSERT INTO "+metric.getMetric()+" ("+columns+
		          ") VALUES ("+values+");";
		//string query="SELECT * FROM "+metric.getMetric()+";";
		lg->db->query(query);
	}
	//otherwise, we need to do averaging
	//check whether the previous period has concluded
	else {
		if(lg->lastTime==0) lg->lastTime=floorTimestamp(metric.getTimestamp(),lg->delay);

		//handle missing data (i.e. Received data at times 2 and 4, but not 3)
		long long f=floorTimestamp(metric.getTimestamp(),lg->delay);
		/*
		===DISABLE LOGGING FOR BAD DATA
		if(lg->lastTime+4*lg->delay >= f)
		{//if there are at most four log entries to write
			while(lg->lastTime+lg->delay < f)
			{
				string columnsBadData=columns;
				lg->lastTime=lg->lastTime+lg->delay;
				ts=floorTimestamp(lg->lastTime,lg->delay)/1000.0;
				stringstream temp2;
				temp2<<fixed<<showpoint<<setprecision(3);
				temp2<<"FROM_UNIXTIME("<<ts<<")";
				string x=itos(metric.getSegment());
				temp2<<", "+x;
				stringstream temp3;
				string valuesBadData;
				valuesBadData=temp2.str();
				int i=0;
				for(auto it=metric.get().begin();it!=metric.get().end();++it,++i)
				{
					columnsBadData=columnsBadData+", "+it->first;
					valuesBadData=valuesBadData+", "+itos(lg->lastWrites[i]);
				}
				string query="INSERT INTO "+metric.getMetric()+" ("+columnsBadData+
					  ", badData) VALUES ("+valuesBadData+", 1);";
				lg->db->query(query);
			} 
		}
		else if(lg->lastTime+4*lg->delay >= f)
		{
			//too big a gap for bad data, so we will skip it
			lg->lastTime=f;
		}*/

		if(lg->lastTime< floorTimestamp(metric.getTimestamp(),lg->delay))
		{
			int i=0;
			for(auto it=metric.get().begin();it!=metric.get().end();++it,++i)
			{
				lg->lastWrites[i]=0;
				int j=0;
				for(;j<lg->history[i].size();++j)
				{
					lg->lastWrites[i]+=lg->history[i][j];
				}
				lg->history[i].resize(0);
				lg->lastWrites[i]=lg->lastWrites[i]/j;
			}

			int k=0;
			for(auto it=keys.begin();it!=keys.end();++it,++k)
			{
				columns=columns+", "+it->first;
				values=values+", "+itos(lg->lastWrites[k]);
			}
			string query="INSERT INTO "+metric.getMetric()+" ("+columns+
		          ") VALUES ("+values+");";
			//string query="SELECT * FROM "+metric.getMetric()+";";
			lg->db->query(query);

			lg->lastTime=floorTimestamp(metric.getTimestamp(),lg->delay);
		}

		if(lg->lastTime==floorTimestamp(metric.getTimestamp(),lg->delay))
		{
			int i=0;
			for(auto it=metric.get().begin();it!=metric.get().end();++it,++i)
			{
				lg->history[i].push_back(stod(it->second));
			}
		}
	}
}
struct invstruct
{
	CRITICAL_SECTION critsec;
	string data;
};
inline bool invhandler(const string& str, void* is_void)
{
	if(str[0]!='#')
	{
		invstruct* is=(invstruct*)is_void;
		EnterCriticalSection(&is->critsec);
		is->data=is->data+str;
		LeaveCriticalSection(&is->critsec);
		return true;
	} else {
		return false;
	}
}
struct verstruct
{
	string version;
	CRITICAL_SECTION critsec;
};
inline bool verhandler(const string& str, void* vs_void)
{
	if(str.substr(0,8)=="#version")
	{
		verstruct* vs=(verstruct*)vs_void;
		EnterCriticalSection(&vs->critsec);
		vs->version=str.substr(9,string::npos);
		LeaveCriticalSection(&vs->critsec);
		return true;
	} else {
		return false;
	}
}