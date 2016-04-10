#ifndef TELEMETRY_H
#define TELEMETRY_H
#include <windows.h>
#include <process.h>
#include <functionlib.h>
#include <childProcess.h>
#include <solarException.h>
#include <map>
#include <vector>
class IsNewline
{
public: bool operator()(char c)
	{
		if(c=='\n' || c=='\r') return true;
		return false;
	}
};

InheritException(solarException,TelemetryException);
InheritException(TelemetryException,TelemetryNotOpen);
InheritException(TelemetryException,MissingHandlerException);
InheritException(TelemetryException,UnparseableMessageStreamUndefinedException);

class Telemetric
{
protected:
	string metric;
	string type;
	long long int timestamp;
	map<string,string> key;
	int segID;
public:
	Telemetric(){}
	Telemetric(string message)
	{
		parse(message);
	}
	bool parse(string message)
	{
		key.clear();
		if(message[0]!='$') return false;
		vector<string> argv=explode(message,' ',2);
		stringstream temp;
		temp<<argv[0];
		temp>>timestamp;
		temp.clear();
		temp<<argv[1];
		temp>>segID;
		temp.clear();
		metric=argv[2];
		type=argv[3];
		size_t size=argv.size();
		for(int i=4;i<size;i+=2)
		{
			if(argv[i].empty() || argv[i+1].empty()) continue;
			key[argv[i]]=argv[i+1];
		}
		return true;
	}
	const string& getType(){return type;}
	const string& getMetric(){return metric;}
	long long int getTimestamp(){return timestamp;}
	int getSegment(){return segID;}
	const string& get(const string& k)
	{
		return key[k];
	}
	const string& operator[](const string& k)
	{
		return get(k);
	}
	const map<string,string>& get()
	{
		return key;
	}
};
typedef void(*telemetricHandler)(Telemetric&,void*);
typedef bool(*invalidHandler)(const string&,void*);
typedef pair<telemetricHandler,void*> hPair;
typedef pair<invalidHandler,void*> ihPair;
class Telemeter
{
protected:
	string javapath;
	string jarpath;
	Process* VDCMon;
	map<string,hPair> messageHandlers;
	vector<ihPair> invalidHandlers;
	stringstream unparseable;
	CRITICAL_SECTION critsec;
	string version;
	static void receiverThread(void* telemeterPtr)
	{
		Telemeter* t=(Telemeter*)telemeterPtr;
		if(!t->is_open()) throw TelemetryNotOpen();
		while(true)
		{
			string message=t->VDCMon->read(IsNewline());
			Telemetric metric;
			bool success=metric.parse(message);
			if(!success)
			{
				EnterCriticalSection(&t->critsec);
				for(int i=0;i<t->invalidHandlers.size();++i)
				{
					try{
						ihPair& p=t->invalidHandlers.at(i);
						p.first(message,p.second);
					} catch (...){/**/}
				}
				LeaveCriticalSection(&t->critsec);
				continue;
			}
			const string& m=metric.getMetric();
			if(m=="") continue;
			hPair& handler=t->messageHandlers[m];
			if(handler.first)
			{
				handler.first(metric,handler.second);
			} else {
				throw MissingHandlerException(const_cast<char*>(m.c_str()));
			}
		}
	}
public:
	Telemeter():VDCMon(NULL){
		InitializeCriticalSection(&critsec);
	};
	Telemeter(string jarpath,string javapath=string(),int port=-1):VDCMon(NULL)
	{
		InitializeCriticalSection(&critsec);
		open(jarpath,javapath,port);
	}
	virtual bool open(string jarpath, string javapath=string(),int port=-1)
	{
		if(!javapath.empty())
			this->javapath=parse_env(javapath);
		else
			this->javapath="java";
		this->jarpath=parse_env(jarpath);
		string command=this->javapath + " -jar \"" + this->jarpath + "\"";
		if(port!=-1)
		{
			command=command+" port="+itos(port);
		}
		VDCMon=new Process(command);
		if(!VDCMon->is_open())
		{
			throw TelemetryNotOpen("Telemetry could not be opened");
		}
		_beginthread(receiverThread,0,(void*)this);
		return VDCMon->is_open();
	}
	virtual bool is_open()
	{
		if(!VDCMon) return false;
		return VDCMon->is_open();
	}
	virtual void close()
	{
		if(VDCMon) delete VDCMon;
	}
	virtual bool callback(string metric, vector<string> keys, telemetricHandler handler, void* param=NULL)
	{
		messageHandlers[metric]=hPair(handler,param);
		hPair p=messageHandlers[metric];
		string command=metric;
		for(int i=0;i<keys.size();++i)
		{
			command+=" ";
			command+=keys[i];
		}
		command+="\n";
		VDCMon->write(command);
		return true;
	}
	virtual bool invalidCallback(invalidHandler handler,void* ptr)
	{
		//This function is used to register handlers for non-message data from the VDCMon.
		//Functions are called in order of registration and passed the invalid data.
		//if they return true, they are considered to have "handled" the message and
		//no further handlers are called.
		//If they return false, the next handler is called.
		ihPair p;
		p.first=handler; p.second=ptr;
		invalidHandlers.push_back(p);
		return true;
	}
	virtual ~Telemeter()
	{
		close();
	}
	double requestVersion()
	{
		if(!is_open()) return false;
		VDCMon->write("#getVersion");
	}
};
#endif // TELEMETRY_H
