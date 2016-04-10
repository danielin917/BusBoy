#pragma once
#include <INI.h>
#include <solarException.h>
#include "Form1.h"
#include "configurator.h"
#include <algorithm>
#include <mysql.h>
#include "../../../Telemetry\nativeConnector\nativeConnector\Telemetry.h"
#include "logging.h"

namespace TelemLogger {

	using namespace System;
	using namespace System::ComponentModel;
	using namespace System::Collections;
	using namespace System::Windows::Forms;
	using namespace System::Data;
	using namespace System::Drawing;

	/// <summary>
	/// Summary for main_form
	/// </summary>
	public ref class main_form : public System::Windows::Forms::Form
	{
	public:
		main_form(void)
		{
			InitializeComponent();
			solarConfig=NULL;
			db=NULL;
			telemeter=NULL;
			lastTime=NULL;
			is=new invstruct();
			vs=new verstruct();
			lastTime=new long long();
			firstTimeOffset=0;
			*lastTime=0;
			driftCount=0;
			driftThreshold=5000;
			
			//initialization done in main_form_Load()
		}

		string Native(System::String^ str)
		{
			IntPtr ptr = System::Runtime::InteropServices::Marshal::StringToHGlobalAnsi(str);
			string ret=(char*)ptr.ToPointer();
			System::Runtime::InteropServices::Marshal::FreeHGlobal(ptr);
			return ret;
		}

	protected:
		System::String^ profileURL;
		INI* solarConfig;
		Telemeter* telemeter;
		mysql* db;
		invstruct* is;
		verstruct* vs;

		//clogged tube detection
		long long firstTimeOffset;
		long long* lastTime;
		int driftCount;
		int driftThreshold; 
		
		//no telems detection
		long long lastTimeConfirmed;
		long long noTelemsCount;

		vector<logger_struct*>* logger_structs;

	private: System::Windows::Forms::Button^  newProfileButton;
	private: System::Windows::Forms::StatusStrip^  statusStrip1;
	private: System::Windows::Forms::RichTextBox^  logBox;
	private: System::Windows::Forms::Timer^  logboxTimer;
	private: System::Windows::Forms::Timer^  versionTimer;
	private: System::Windows::Forms::TextBox^  portNum;
	private: System::Windows::Forms::Label^  portNumLabel;




	private: System::Windows::Forms::ToolStripStatusLabel^  errLabel;






	protected: 
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		void cleanup()
		{
			if(db) delete db; db=NULL;
			if(telemeter) delete telemeter; telemeter=NULL;
			if(logger_structs)
			{
				for(int i=0;i<logger_structs->size();++i)
				{
					if((*logger_structs)[i]) delete (*logger_structs)[i];
				}
				delete logger_structs; logger_structs=NULL;
			}
		}

		~main_form()
		{
			if (components)
			{
				delete components;
			}
			cleanup();
			if(solarConfig)
			{
				solarConfig->save();
				delete solarConfig;
			}
			if(is) delete is;
			if(vs) delete vs;
			if(lastTime) delete lastTime;
		}
	private: System::Windows::Forms::GroupBox^  groupBox1;
	protected: 
	private: System::Windows::Forms::Button^  profileBrowseButton;
	private: System::Windows::Forms::TextBox^  profileString;

	private: System::Windows::Forms::Button^  button1;


	private: System::Windows::Forms::Button^  runButton;
	private: System::ComponentModel::IContainer^  components;

	private:
		/// <summary>
		/// Required designer variable.
		/// </summary>


#pragma region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		void InitializeComponent(void)
		{
			this->components = (gcnew System::ComponentModel::Container());
			System::ComponentModel::ComponentResourceManager^  resources = (gcnew System::ComponentModel::ComponentResourceManager(main_form::typeid));
			this->groupBox1 = (gcnew System::Windows::Forms::GroupBox());
			this->newProfileButton = (gcnew System::Windows::Forms::Button());
			this->profileBrowseButton = (gcnew System::Windows::Forms::Button());
			this->profileString = (gcnew System::Windows::Forms::TextBox());
			this->button1 = (gcnew System::Windows::Forms::Button());
			this->runButton = (gcnew System::Windows::Forms::Button());
			this->statusStrip1 = (gcnew System::Windows::Forms::StatusStrip());
			this->errLabel = (gcnew System::Windows::Forms::ToolStripStatusLabel());
			this->logBox = (gcnew System::Windows::Forms::RichTextBox());
			this->logboxTimer = (gcnew System::Windows::Forms::Timer(this->components));
			this->versionTimer = (gcnew System::Windows::Forms::Timer(this->components));
			this->portNum = (gcnew System::Windows::Forms::TextBox());
			this->portNumLabel = (gcnew System::Windows::Forms::Label());
			this->groupBox1->SuspendLayout();
			this->statusStrip1->SuspendLayout();
			this->SuspendLayout();
			// 
			// groupBox1
			// 
			this->groupBox1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox1->Controls->Add(this->newProfileButton);
			this->groupBox1->Controls->Add(this->profileBrowseButton);
			this->groupBox1->Controls->Add(this->profileString);
			this->groupBox1->Location = System::Drawing::Point(13, 13);
			this->groupBox1->Name = L"groupBox1";
			this->groupBox1->Size = System::Drawing::Size(409, 52);
			this->groupBox1->TabIndex = 0;
			this->groupBox1->TabStop = false;
			this->groupBox1->Text = L"Logging Profile";
			// 
			// newProfileButton
			// 
			this->newProfileButton->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->newProfileButton->Location = System::Drawing::Point(328, 18);
			this->newProfileButton->Name = L"newProfileButton";
			this->newProfileButton->Size = System::Drawing::Size(75, 23);
			this->newProfileButton->TabIndex = 2;
			this->newProfileButton->Text = L"Create New";
			this->newProfileButton->UseVisualStyleBackColor = true;
			this->newProfileButton->Click += gcnew System::EventHandler(this, &main_form::newProfileButton_Click);
			// 
			// profileBrowseButton
			// 
			this->profileBrowseButton->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->profileBrowseButton->Location = System::Drawing::Point(247, 18);
			this->profileBrowseButton->Name = L"profileBrowseButton";
			this->profileBrowseButton->Size = System::Drawing::Size(75, 23);
			this->profileBrowseButton->TabIndex = 1;
			this->profileBrowseButton->Text = L"Browse";
			this->profileBrowseButton->UseVisualStyleBackColor = true;
			this->profileBrowseButton->Click += gcnew System::EventHandler(this, &main_form::profileBrowseButton_Click);
			// 
			// profileString
			// 
			this->profileString->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->profileString->Location = System::Drawing::Point(6, 20);
			this->profileString->Name = L"profileString";
			this->profileString->Size = System::Drawing::Size(235, 20);
			this->profileString->TabIndex = 0;
			this->profileString->TextChanged += gcnew System::EventHandler(this, &main_form::profileString_TextChanged);
			// 
			// button1
			// 
			this->button1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->button1->Location = System::Drawing::Point(341, 69);
			this->button1->Name = L"button1";
			this->button1->Size = System::Drawing::Size(75, 23);
			this->button1->TabIndex = 1;
			this->button1->Text = L"Settings";
			this->button1->UseVisualStyleBackColor = true;
			this->button1->Click += gcnew System::EventHandler(this, &main_form::button1_Click);
			// 
			// runButton
			// 
			this->runButton->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->runButton->Location = System::Drawing::Point(341, 201);
			this->runButton->Name = L"runButton";
			this->runButton->Size = System::Drawing::Size(75, 23);
			this->runButton->TabIndex = 4;
			this->runButton->Text = L"Run";
			this->runButton->UseVisualStyleBackColor = true;
			this->runButton->Click += gcnew System::EventHandler(this, &main_form::runButton_Click);
			// 
			// statusStrip1
			// 
			this->statusStrip1->Items->AddRange(gcnew cli::array< System::Windows::Forms::ToolStripItem^  >(1) {this->errLabel});
			this->statusStrip1->Location = System::Drawing::Point(0, 234);
			this->statusStrip1->Name = L"statusStrip1";
			this->statusStrip1->Size = System::Drawing::Size(434, 22);
			this->statusStrip1->TabIndex = 5;
			this->statusStrip1->Text = L"statusStrip1";
			// 
			// errLabel
			// 
			this->errLabel->Name = L"errLabel";
			this->errLabel->Size = System::Drawing::Size(0, 17);
			// 
			// logBox
			// 
			this->logBox->BackColor = System::Drawing::SystemColors::Window;
			this->logBox->Location = System::Drawing::Point(13, 71);
			this->logBox->Name = L"logBox";
			this->logBox->ReadOnly = true;
			this->logBox->Size = System::Drawing::Size(322, 153);
			this->logBox->TabIndex = 6;
			this->logBox->Text = L"";
			// 
			// logboxTimer
			// 
			this->logboxTimer->Tick += gcnew System::EventHandler(this, &main_form::logboxTimer_Tick_1);
			// 
			// versionTimer
			// 
			this->versionTimer->Interval = 5000;
			this->versionTimer->Tick += gcnew System::EventHandler(this, &main_form::versionTimer_Tick);
			// 
			// portNum
			// 
			this->portNum->Location = System::Drawing::Point(342, 115);
			this->portNum->Name = L"portNum";
			this->portNum->Size = System::Drawing::Size(74, 20);
			this->portNum->TabIndex = 7;
			// 
			// portNumLabel
			// 
			this->portNumLabel->AutoSize = true;
			this->portNumLabel->Location = System::Drawing::Point(341, 99);
			this->portNumLabel->Name = L"portNumLabel";
			this->portNumLabel->Size = System::Drawing::Size(26, 13);
			this->portNumLabel->TabIndex = 8;
			this->portNumLabel->Text = L"Port";
			// 
			// main_form
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(434, 256);
			this->Controls->Add(this->portNumLabel);
			this->Controls->Add(this->portNum);
			this->Controls->Add(this->logBox);
			this->Controls->Add(this->statusStrip1);
			this->Controls->Add(this->runButton);
			this->Controls->Add(this->button1);
			this->Controls->Add(this->groupBox1);
			this->Icon = (cli::safe_cast<System::Drawing::Icon^  >(resources->GetObject(L"$this.Icon")));
			this->Name = L"main_form";
			this->Text = L"Telemetry Logger";
			this->Load += gcnew System::EventHandler(this, &main_form::main_form_Load);
			this->groupBox1->ResumeLayout(false);
			this->groupBox1->PerformLayout();
			this->statusStrip1->ResumeLayout(false);
			this->statusStrip1->PerformLayout();
			this->ResumeLayout(false);
			this->PerformLayout();

		}
#pragma endregion
	private: System::Void runButton_Click(System::Object^  sender, System::EventArgs^  e) {
				errLabel->Text="";
				//reload the configuration, in case it's changed
				//this is a terrible way to do this
				//actually, this does very little now :D
				cleanup();
				load();
				try{
					string& url=(*solarConfig)["mysql"]["url"];
					string& username=(*solarConfig)["mysql"]["username"];
					string& password=(*solarConfig)["mysql"]["password"];
					string& database=(*solarConfig)["mysql/telemetry"]["db"];
					string& javapath=(*solarConfig)["java"]["javapath"];
					string& jarpath=(*solarConfig)["telemetry"]["jarpath"];
					db=new mysql(url,username,password,database);
					telemeter=new Telemeter();
					//make sure we're using the right version
					telemeter->invalidCallback(verhandler,vs);
					int portnum=-1;
					if(portNum->Text!="")
					{
						portnum=System::Convert::ToInt32(portNum->Text);
					}
					telemeter->open(jarpath,javapath,portnum);
					versionTimer->Enabled=true;
				} catch (solarException e)
				{
					errLabel->Text=gcnew String(e.error);
					return;
				}

				logger_structs = new vector<logger_struct*>;
				//read file
				string fn=Native(profileString->Text);
				ifstream infile(fn);
				if(!infile.is_open())
				{
					errLabel->Text="Could not open logging profile";
					return;
				}
				string metric;
				while(infile>>metric)
				{
					if(metric=="") break;

					//initialize logger parameters
					logger_struct* lg=new logger_struct;
					lg->db=db;
					double temp;
					infile>>temp;
					lg->delay=1000.0*temp;
					lg->lastTime=0;
					lg->globalLastTime=lastTime;
					int size=10;///FIX
					lg->lastWrites.resize(10,0);
					lg->history.resize(10,vector<double>(0));
					logger_structs->push_back(lg);

					//prepare to start the callback
					string line_remainder;
					getline(infile,line_remainder,'\n');
					vector<string> keys=explode(line_remainder,' ',1);

					//create database column (fails if already exists, simply swallow)
					try{
						string collist;
						for(int i=0;i<keys.size();++i)
						{
							collist=collist+", "+keys[i]+" DOUBLE";
						}
						string query="CREATE TABLE "+metric+" (vdctime TIMESTAMP(3)"+collist+", badData TINYINT(1));";
						db->query(query);
					} catch (...){}
					try{
						string query="ALTER TABLE "+metric+" ALTER badData SET DEFAULT 0;";
						db->query(query);
					} catch (...){}
					try{
						string query="ALTER TABLE "+metric+" ADD COLUMN segId BIGINT(16);";
						db->query(query);
					} catch (...){}
					telemeter->callback(metric,keys,logTelemToDb,lg);
				}
				
				//begin redirecting invalid lines to console
				InitializeCriticalSection(&is->critsec);
				telemeter->invalidCallback(invhandler,(void*)is);
				logboxTimer->Enabled=true;

				runButton->Enabled=false;
				button1->Enabled=false;
			 }
private: System::Void button1_Click(System::Object^  sender, System::EventArgs^  e) {
			 configurator^ f=gcnew configurator(solarConfig);
			 f->ShowDialog();
			 errLabel->Text=gcnew String("");
			 load();
		 }
		 void load()
		 {
			string& url=(*solarConfig)["mysql"]["url"];
			if(url=="") url="localhost";
			string& username=(*solarConfig)["mysql"]["username"];
			if(username=="") username="root";
			string& password=(*solarConfig)["mysql"]["password"];
			if(password=="") password="NO_PASSWORD";
			string& database=(*solarConfig)["mysql/telemetry"]["db"];
			if(database=="") database="telemetry";
			profileURL=gcnew System::String((*solarConfig)["telemetry/logger"]["profile"].c_str());
			string& javapath=(*solarConfig)["java"]["javapath"];
			if(javapath=="") javapath="javaw";
			string& jarpath=(*solarConfig)["telemetry"]["jarpath"];
			if(jarpath=="") jarpath="VDCMon.jar";
			solarConfig->save();
		 }
private: System::Void main_form_Load(System::Object^  sender, System::EventArgs^  e) {
			try
			{
				solarConfig=new INI("%appdata%/Solar/config.ini");
				string pid=itos(GetCurrentProcessId());
				string fn=parse_env("%appdata%/Solar/TelemLoggerLogfile_"+pid+".txt");
				load();
			}
			catch(solarException e)
			{
				errLabel->Text=gcnew System::String (e.error);
				return;
			}
		 }
private: System::Void newProfileButton_Click(System::Object^  sender, System::EventArgs^  e) {
			 Form1^ f=gcnew Form1(profileString->Text,solarConfig);
			 f->Show();
			 this->profileString->Text=f->getFileName();
		 }
private: System::Void profileString_TextChanged(System::Object^  sender, System::EventArgs^  e) {
		 }
private: System::Void profileBrowseButton_Click(System::Object^  sender, System::EventArgs^  e) {
			 OpenFileDialog^ fd=gcnew OpenFileDialog();
			 fd->Filter="Telemetry Monitoring Profiles (*.scttmm)|*.scttmm";
			 fd->RestoreDirectory=true;
			 if ( fd->ShowDialog() == System::Windows::Forms::DialogResult::OK )
			 {
				 this->profileString->Text=fd->FileName;
			 }
		 }
public: System::Void printColor(String^ message, Color color)
		{
			logBox->SelectionStart=logBox->TextLength;
			logBox->SelectionLength=0;
			logBox->SelectionColor=color;
			logBox->AppendText(message);
			logBox->SelectionColor=logBox->ForeColor;
		}
private: System::Void logboxTimer_Tick_1(System::Object^  sender, System::EventArgs^  e) {
			 int lt=*lastTime;
			 long long now=DateTime::Now.Ticks/10000;
			 EnterCriticalSection(&is->critsec);
			 if(!is->data.empty())
			 {
				 logBox->AppendText( Environment::NewLine + gcnew String((is->data).c_str()) );
				 is->data="";
				 noTelemsCount =  0;
			 }
			 LeaveCriticalSection(&is->critsec);

			 if(lastTimeConfirmed==lt)
			 {
				 noTelemsCount++;
				 //printColor(Environment::NewLine + noTelemsCount, Color::Blue);
				 if(noTelemsCount>=50)//5 seconds
				 {
					 printColor(Environment::NewLine + "No Telemetry" ,Color::Red);
					 noTelemsCount=0;
					 return;
				 }
			 }else {
				 noTelemsCount=0;
				 lastTimeConfirmed=lt;
			 }
			 if(lt!=0 && lt!=lastTimeConfirmed)
			 {
				 lastTimeConfirmed=lt;
				 if(firstTimeOffset==0)
				 {	 //initialize the offset - determine how much the local 
					 //machine disagrees with the server
					 firstTimeOffset=now-lt;
					 return;
				 }
				 if(lt+firstTimeOffset+driftThreshold<now)
				 {
					 driftCount++;
				 } else {
					 driftCount=0;
				 }
				 if(driftCount>10)
				 {
					 printColor(Environment::NewLine + "The Pipes Are Clogged! Lag(ms): " + gcnew String(itos(now-(lt+firstTimeOffset)).c_str())
						 ,Color::Red);
					 driftCount=0;
				 }
			 }

		 }
private: System::Void versionTimer_Tick(System::Object^  sender, System::EventArgs^  e) {
			 if(telemeter->is_open())
			 {
				 string requiredVersion="1.3";
				 if(vs->version==requiredVersion)
				 {
					logBox->AppendText( Environment::NewLine + gcnew String(("Using VDCMon version "+vs->version).c_str()) );
				
				 }
				 else
				 {
					 if(vs->version!="")
					 {
						 printColor(Environment::NewLine + "Using VDCMon Version "+(gcnew String(vs->version.c_str()))
							 ,Color::Red);
					 }
					 printColor(Environment::NewLine + "Requires VDCMon Version "+(gcnew String(requiredVersion.c_str()))
							 ,Color::Red);
					 errLabel->Text="Incorrect VDCMon version";
					 cleanup();
					 runButton->Enabled=true;
					 button1->Enabled=true;
					 logboxTimer->Enabled=false;
				 }
			 }
			 versionTimer->Enabled=false;
		 }
};
}
