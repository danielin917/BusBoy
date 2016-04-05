#pragma once
#include <windows.h> 
#include <windowsx.h> 
#include <fstream>
#include <stdio.h> 
#include "Resource.h" 
#include <string>
#include <iostream>
#include <stdio.h>
#include "../../../Tools/include/cpp/INI.h"
#include <msclr\marshal_cppstd.h>
using namespace std;

using namespace msclr::interop;
using namespace std;
namespace TelemLogger {

	using namespace System;
	using namespace System::ComponentModel;
	using namespace System::Collections;
	using namespace System::Windows::Forms;
	using namespace System::Data;
	using namespace System::Drawing;

	/// <summary>
	/// Summary for configurator
	/// </summary>
	public ref class configurator : public System::Windows::Forms::Form
	{
	
	public:
		INI* solarConfig;
		configurator(INI* solarConfig)
		{
			InitializeComponent();
			this->solarConfig=solarConfig;
		}

	protected:
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		~configurator()
		{
			delete solarConfig;
			if (components)
			{
				delete components;
			}
		}
	private: System::Windows::Forms::TextBox^  PHPLocation;
	private: System::Windows::Forms::Button^  save;


	protected: 




	private: System::Windows::Forms::Label^  label1;
	private: System::Windows::Forms::GroupBox^  groupBox1;
	private: System::Windows::Forms::HelpProvider^  helpProvider1;
	private: System::Windows::Forms::GroupBox^  groupBox2;
	private: System::Windows::Forms::Label^  label3;
	private: System::Windows::Forms::TextBox^  Username;


	private: System::Windows::Forms::Label^  label2;
	private: System::Windows::Forms::TextBox^  URL;


	private: System::Windows::Forms::Label^  label4;
	private: System::Windows::Forms::TextBox^  Password;

	private: System::Windows::Forms::FolderBrowserDialog^  folderBrowser;
	private: System::Windows::Forms::Button^  browsePHPLocation;

	private: System::Windows::Forms::Label^  label5;
	private: System::Windows::Forms::TextBox^  GPSDatabase;
	private: System::Windows::Forms::Button^  cancel;



	private: System::Windows::Forms::Button^  okay;
	private: System::Windows::Forms::StatusStrip^  statusStrip;
	private: System::Windows::Forms::ToolStripStatusLabel^  toolStripStatusLabel;
	private: System::Windows::Forms::GroupBox^  groupBox3;
	private: System::Windows::Forms::Button^  browseCANLocation;

	private: System::Windows::Forms::Label^  label6;
	private: System::Windows::Forms::TextBox^  CANStructLocation;

	private: System::Windows::Forms::GroupBox^  groupBox4;
	private: System::Windows::Forms::Button^  browseJavawButton;

	private: System::Windows::Forms::Label^  label8;
	private: System::Windows::Forms::TextBox^  javawLocation;

	private: System::Windows::Forms::Button^  browseJarLocation;

	private: System::Windows::Forms::Label^  label7;
	private: System::Windows::Forms::TextBox^  jarLocation;







	protected: 

	private:
		/// <summary>
		/// Required designer variable.
		/// </summary>
		System::ComponentModel::Container ^components;
#pragma region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		void InitializeComponent(void)
		{
			this->PHPLocation = (gcnew System::Windows::Forms::TextBox());
			this->save = (gcnew System::Windows::Forms::Button());
			this->label1 = (gcnew System::Windows::Forms::Label());
			this->groupBox1 = (gcnew System::Windows::Forms::GroupBox());
			this->browsePHPLocation = (gcnew System::Windows::Forms::Button());
			this->helpProvider1 = (gcnew System::Windows::Forms::HelpProvider());
			this->groupBox2 = (gcnew System::Windows::Forms::GroupBox());
			this->label5 = (gcnew System::Windows::Forms::Label());
			this->GPSDatabase = (gcnew System::Windows::Forms::TextBox());
			this->label4 = (gcnew System::Windows::Forms::Label());
			this->Password = (gcnew System::Windows::Forms::TextBox());
			this->label3 = (gcnew System::Windows::Forms::Label());
			this->Username = (gcnew System::Windows::Forms::TextBox());
			this->label2 = (gcnew System::Windows::Forms::Label());
			this->URL = (gcnew System::Windows::Forms::TextBox());
			this->folderBrowser = (gcnew System::Windows::Forms::FolderBrowserDialog());
			this->cancel = (gcnew System::Windows::Forms::Button());
			this->okay = (gcnew System::Windows::Forms::Button());
			this->statusStrip = (gcnew System::Windows::Forms::StatusStrip());
			this->toolStripStatusLabel = (gcnew System::Windows::Forms::ToolStripStatusLabel());
			this->groupBox3 = (gcnew System::Windows::Forms::GroupBox());
			this->browseCANLocation = (gcnew System::Windows::Forms::Button());
			this->label6 = (gcnew System::Windows::Forms::Label());
			this->CANStructLocation = (gcnew System::Windows::Forms::TextBox());
			this->groupBox4 = (gcnew System::Windows::Forms::GroupBox());
			this->browseJavawButton = (gcnew System::Windows::Forms::Button());
			this->label8 = (gcnew System::Windows::Forms::Label());
			this->javawLocation = (gcnew System::Windows::Forms::TextBox());
			this->browseJarLocation = (gcnew System::Windows::Forms::Button());
			this->label7 = (gcnew System::Windows::Forms::Label());
			this->jarLocation = (gcnew System::Windows::Forms::TextBox());
			this->groupBox1->SuspendLayout();
			this->groupBox2->SuspendLayout();
			this->statusStrip->SuspendLayout();
			this->groupBox3->SuspendLayout();
			this->groupBox4->SuspendLayout();
			this->SuspendLayout();
			// 
			// PHPLocation
			// 
			this->PHPLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->PHPLocation->Location = System::Drawing::Point(88, 17);
			this->PHPLocation->Name = L"PHPLocation";
			this->PHPLocation->Size = System::Drawing::Size(248, 20);
			this->PHPLocation->TabIndex = 1;
			this->PHPLocation->TextChanged += gcnew System::EventHandler(this, &configurator::PHPLocation_TextChanged);
			// 
			// save
			// 
			this->save->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->save->Enabled = false;
			this->save->Location = System::Drawing::Point(369, 327);
			this->save->Name = L"save";
			this->save->Size = System::Drawing::Size(75, 23);
			this->save->TabIndex = 2;
			this->save->Text = L"Apply";
			this->save->UseVisualStyleBackColor = true;
			this->save->Click += gcnew System::EventHandler(this, &configurator::save_Click);
			// 
			// label1
			// 
			this->label1->AutoSize = true;
			this->label1->Location = System::Drawing::Point(6, 20);
			this->label1->Name = L"label1";
			this->label1->Size = System::Drawing::Size(76, 13);
			this->label1->TabIndex = 3;
			this->label1->Text = L"PHP Location:";
			// 
			// groupBox1
			// 
			this->groupBox1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox1->Controls->Add(this->browsePHPLocation);
			this->groupBox1->Controls->Add(this->label1);
			this->groupBox1->Controls->Add(this->PHPLocation);
			this->groupBox1->Location = System::Drawing::Point(12, 12);
			this->groupBox1->Name = L"groupBox1";
			this->groupBox1->Size = System::Drawing::Size(432, 47);
			this->groupBox1->TabIndex = 4;
			this->groupBox1->TabStop = false;
			this->groupBox1->Text = L"Environment";
			// 
			// browsePHPLocation
			// 
			this->browsePHPLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->browsePHPLocation->Location = System::Drawing::Point(342, 15);
			this->browsePHPLocation->Name = L"browsePHPLocation";
			this->browsePHPLocation->Size = System::Drawing::Size(84, 23);
			this->browsePHPLocation->TabIndex = 4;
			this->browsePHPLocation->Text = L"Browse...";
			this->browsePHPLocation->UseVisualStyleBackColor = true;
			this->browsePHPLocation->Click += gcnew System::EventHandler(this, &configurator::browsePHPLocation_Click);
			// 
			// groupBox2
			// 
			this->groupBox2->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox2->Controls->Add(this->label5);
			this->groupBox2->Controls->Add(this->GPSDatabase);
			this->groupBox2->Controls->Add(this->label4);
			this->groupBox2->Controls->Add(this->Password);
			this->groupBox2->Controls->Add(this->label3);
			this->groupBox2->Controls->Add(this->Username);
			this->groupBox2->Controls->Add(this->label2);
			this->groupBox2->Controls->Add(this->URL);
			this->groupBox2->Location = System::Drawing::Point(12, 65);
			this->groupBox2->Name = L"groupBox2";
			this->groupBox2->Size = System::Drawing::Size(432, 122);
			this->groupBox2->TabIndex = 5;
			this->groupBox2->TabStop = false;
			this->groupBox2->Text = L"MySQL";
			// 
			// label5
			// 
			this->label5->AutoSize = true;
			this->label5->Location = System::Drawing::Point(6, 98);
			this->label5->Name = L"label5";
			this->label5->Size = System::Drawing::Size(56, 13);
			this->label5->TabIndex = 9;
			this->label5->Text = L"Database:";
			// 
			// GPSDatabase
			// 
			this->GPSDatabase->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->GPSDatabase->Location = System::Drawing::Point(88, 95);
			this->GPSDatabase->Name = L"GPSDatabase";
			this->GPSDatabase->Size = System::Drawing::Size(338, 20);
			this->GPSDatabase->TabIndex = 8;
			this->GPSDatabase->TextChanged += gcnew System::EventHandler(this, &configurator::GPSDatabase_TextChanged);
			// 
			// label4
			// 
			this->label4->AutoSize = true;
			this->label4->Location = System::Drawing::Point(6, 72);
			this->label4->Name = L"label4";
			this->label4->Size = System::Drawing::Size(56, 13);
			this->label4->TabIndex = 7;
			this->label4->Text = L"Password:";
			// 
			// Password
			// 
			this->Password->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->Password->Location = System::Drawing::Point(88, 69);
			this->Password->Name = L"Password";
			this->Password->Size = System::Drawing::Size(338, 20);
			this->Password->TabIndex = 6;
			this->Password->TextChanged += gcnew System::EventHandler(this, &configurator::Password_TextChanged);
			// 
			// label3
			// 
			this->label3->AutoSize = true;
			this->label3->Location = System::Drawing::Point(6, 46);
			this->label3->Name = L"label3";
			this->label3->Size = System::Drawing::Size(58, 13);
			this->label3->TabIndex = 5;
			this->label3->Text = L"Username:";
			// 
			// Username
			// 
			this->Username->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->Username->Location = System::Drawing::Point(88, 43);
			this->Username->Name = L"Username";
			this->Username->Size = System::Drawing::Size(338, 20);
			this->Username->TabIndex = 4;
			this->Username->TextChanged += gcnew System::EventHandler(this, &configurator::Username_TextChanged);
			// 
			// label2
			// 
			this->label2->AutoSize = true;
			this->label2->Location = System::Drawing::Point(6, 24);
			this->label2->Name = L"label2";
			this->label2->Size = System::Drawing::Size(32, 13);
			this->label2->TabIndex = 3;
			this->label2->Text = L"URL:";
			// 
			// URL
			// 
			this->URL->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->URL->Location = System::Drawing::Point(88, 17);
			this->URL->Name = L"URL";
			this->URL->Size = System::Drawing::Size(338, 20);
			this->URL->TabIndex = 1;
			this->URL->TextChanged += gcnew System::EventHandler(this, &configurator::URL_TextChanged);
			// 
			// folderBrowser
			// 
			this->folderBrowser->RootFolder = System::Environment::SpecialFolder::MyComputer;
			// 
			// cancel
			// 
			this->cancel->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->cancel->Location = System::Drawing::Point(288, 327);
			this->cancel->Name = L"cancel";
			this->cancel->Size = System::Drawing::Size(75, 23);
			this->cancel->TabIndex = 6;
			this->cancel->Text = L"Cancel";
			this->cancel->UseVisualStyleBackColor = true;
			this->cancel->Click += gcnew System::EventHandler(this, &configurator::cancel_Click);
			// 
			// okay
			// 
			this->okay->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->okay->Location = System::Drawing::Point(207, 327);
			this->okay->Name = L"okay";
			this->okay->Size = System::Drawing::Size(75, 23);
			this->okay->TabIndex = 10;
			this->okay->Text = L"Okay";
			this->okay->UseVisualStyleBackColor = true;
			this->okay->Click += gcnew System::EventHandler(this, &configurator::okay_Click);
			// 
			// statusStrip
			// 
			this->statusStrip->Items->AddRange(gcnew cli::array< System::Windows::Forms::ToolStripItem^  >(1) {this->toolStripStatusLabel});
			this->statusStrip->Location = System::Drawing::Point(0, 356);
			this->statusStrip->Name = L"statusStrip";
			this->statusStrip->Size = System::Drawing::Size(453, 22);
			this->statusStrip->TabIndex = 11;
			this->statusStrip->Text = L"statusStrip1";
			// 
			// toolStripStatusLabel
			// 
			this->toolStripStatusLabel->Name = L"toolStripStatusLabel";
			this->toolStripStatusLabel->Size = System::Drawing::Size(0, 17);
			// 
			// groupBox3
			// 
			this->groupBox3->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox3->Controls->Add(this->browseCANLocation);
			this->groupBox3->Controls->Add(this->label6);
			this->groupBox3->Controls->Add(this->CANStructLocation);
			this->groupBox3->Location = System::Drawing::Point(12, 193);
			this->groupBox3->Name = L"groupBox3";
			this->groupBox3->Size = System::Drawing::Size(432, 47);
			this->groupBox3->TabIndex = 5;
			this->groupBox3->TabStop = false;
			this->groupBox3->Text = L"Telemetry";
			// 
			// browseCANLocation
			// 
			this->browseCANLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->browseCANLocation->Location = System::Drawing::Point(342, 15);
			this->browseCANLocation->Name = L"browseCANLocation";
			this->browseCANLocation->Size = System::Drawing::Size(84, 23);
			this->browseCANLocation->TabIndex = 4;
			this->browseCANLocation->Text = L"Browse...";
			this->browseCANLocation->UseVisualStyleBackColor = true;
			this->browseCANLocation->Click += gcnew System::EventHandler(this, &configurator::browseRouteFileLocation_Click);
			// 
			// label6
			// 
			this->label6->AutoSize = true;
			this->label6->Location = System::Drawing::Point(6, 20);
			this->label6->Name = L"label6";
			this->label6->Size = System::Drawing::Size(78, 13);
			this->label6->TabIndex = 3;
			this->label6->Text = L"CAN Structure:";
			// 
			// CANStructLocation
			// 
			this->CANStructLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->CANStructLocation->Location = System::Drawing::Point(88, 17);
			this->CANStructLocation->Name = L"CANStructLocation";
			this->CANStructLocation->Size = System::Drawing::Size(248, 20);
			this->CANStructLocation->TabIndex = 1;
			this->CANStructLocation->TextChanged += gcnew System::EventHandler(this, &configurator::routeFileLocation_TextChanged);
			// 
			// groupBox4
			// 
			this->groupBox4->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox4->Controls->Add(this->browseJavawButton);
			this->groupBox4->Controls->Add(this->label8);
			this->groupBox4->Controls->Add(this->javawLocation);
			this->groupBox4->Controls->Add(this->browseJarLocation);
			this->groupBox4->Controls->Add(this->label7);
			this->groupBox4->Controls->Add(this->jarLocation);
			this->groupBox4->Location = System::Drawing::Point(12, 246);
			this->groupBox4->Name = L"groupBox4";
			this->groupBox4->Size = System::Drawing::Size(432, 74);
			this->groupBox4->TabIndex = 6;
			this->groupBox4->TabStop = false;
			this->groupBox4->Text = L"Java";
			// 
			// browseJavawButton
			// 
			this->browseJavawButton->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->browseJavawButton->Location = System::Drawing::Point(342, 44);
			this->browseJavawButton->Name = L"browseJavawButton";
			this->browseJavawButton->Size = System::Drawing::Size(84, 23);
			this->browseJavawButton->TabIndex = 7;
			this->browseJavawButton->Text = L"Browse...";
			this->browseJavawButton->UseVisualStyleBackColor = true;
			this->browseJavawButton->Click += gcnew System::EventHandler(this, &configurator::button2_Click);
			// 
			// label8
			// 
			this->label8->AutoSize = true;
			this->label8->Location = System::Drawing::Point(6, 49);
			this->label8->Name = L"label8";
			this->label8->Size = System::Drawing::Size(72, 13);
			this->label8->TabIndex = 6;
			this->label8->Text = L"Java(w) Path:";
			// 
			// javawLocation
			// 
			this->javawLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->javawLocation->Location = System::Drawing::Point(88, 46);
			this->javawLocation->Name = L"javawLocation";
			this->javawLocation->Size = System::Drawing::Size(248, 20);
			this->javawLocation->TabIndex = 5;
			this->javawLocation->TextChanged += gcnew System::EventHandler(this, &configurator::javawLocation_TextChanged);
			// 
			// browseJarLocation
			// 
			this->browseJarLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Right));
			this->browseJarLocation->Location = System::Drawing::Point(342, 15);
			this->browseJarLocation->Name = L"browseJarLocation";
			this->browseJarLocation->Size = System::Drawing::Size(84, 23);
			this->browseJarLocation->TabIndex = 4;
			this->browseJarLocation->Text = L"Browse...";
			this->browseJarLocation->UseVisualStyleBackColor = true;
			this->browseJarLocation->Click += gcnew System::EventHandler(this, &configurator::browseJarLocation_Click);
			// 
			// label7
			// 
			this->label7->AutoSize = true;
			this->label7->Location = System::Drawing::Point(6, 20);
			this->label7->Name = L"label7";
			this->label7->Size = System::Drawing::Size(70, 13);
			this->label7->TabIndex = 3;
			this->label7->Text = L"VDCMon Jar:";
			// 
			// jarLocation
			// 
			this->jarLocation->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->jarLocation->Location = System::Drawing::Point(88, 17);
			this->jarLocation->Name = L"jarLocation";
			this->jarLocation->Size = System::Drawing::Size(248, 20);
			this->jarLocation->TabIndex = 1;
			this->jarLocation->TextChanged += gcnew System::EventHandler(this, &configurator::jarLocation_TextChanged);
			// 
			// configurator
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(453, 378);
			this->Controls->Add(this->groupBox4);
			this->Controls->Add(this->groupBox3);
			this->Controls->Add(this->statusStrip);
			this->Controls->Add(this->okay);
			this->Controls->Add(this->cancel);
			this->Controls->Add(this->groupBox2);
			this->Controls->Add(this->groupBox1);
			this->Controls->Add(this->save);
			this->Name = L"configurator";
			this->Text = L"Configuration";
			this->Load += gcnew System::EventHandler(this, &configurator::configurator_Load);
			this->groupBox1->ResumeLayout(false);
			this->groupBox1->PerformLayout();
			this->groupBox2->ResumeLayout(false);
			this->groupBox2->PerformLayout();
			this->statusStrip->ResumeLayout(false);
			this->statusStrip->PerformLayout();
			this->groupBox3->ResumeLayout(false);
			this->groupBox3->PerformLayout();
			this->groupBox4->ResumeLayout(false);
			this->groupBox4->PerformLayout();
			this->ResumeLayout(false);
			this->PerformLayout();

		}
#pragma endregion
private: System::Void configurator_Load(System::Object^  sender, System::EventArgs^  e) {
				PHPLocation->Text=gcnew String((*solarConfig)["env"]["php_location"].c_str());
				URL->Text=gcnew String((*solarConfig)["mysql"]["url"].c_str());
				Username->Text=gcnew String((*solarConfig)["mysql"]["username"].c_str());
				if((*solarConfig)["mysql"]["password"]=="") (*solarConfig)["mysql"]["password"]="NO_PASSWORD";
				Password->Text=gcnew String((*solarConfig)["mysql"]["password"].c_str());
				GPSDatabase->Text=gcnew String((*solarConfig)["mysql/telemetry"]["db"].c_str());
				CANStructLocation->Text=gcnew String((*solarConfig)["telemetry"]["canstructure"].c_str());
				jarLocation->Text=gcnew String((*solarConfig)["telemetry"]["jarpath"].c_str());
				javawLocation->Text=gcnew String((*solarConfig)["java"]["javapath"].c_str());
				save->Enabled = false;
			 }

private: System::Void browsePHPLocation_Click(System::Object^  sender, System::EventArgs^  e) {
			 System::Windows::Forms::DialogResult result = folderBrowser->ShowDialog();
		     PHPLocation->Text=folderBrowser->SelectedPath;
		 }
private: System::Void cancel_Click(System::Object^  sender, System::EventArgs^  e) {
			 Close();
		 }
private: System::Void button1_Click(System::Object^  sender, System::EventArgs^  e) {
		 }
private: System::Void save_Click(System::Object^  sender, System::EventArgs^  e) {
			 if(solarConfig->save())
			 {
				 save->Enabled=false;
				 toolStripStatusLabel->Text="All changes saved successfully.";
			 } else 
			 {
				 toolStripStatusLabel->Text="Could not save changes";
			 }
		 }
private: System::Void PHPLocation_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("env","php_location",marshal_as<string>(PHPLocation->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void URL_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("mysql","url",marshal_as<string>(URL->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void Username_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("mysql","username",marshal_as<string>(Username->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void Password_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("mysql","password",marshal_as<string>(Password->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void GPSDatabase_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("mysql/telemetry","db",marshal_as<string>(GPSDatabase->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void okay_Click(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->save();
			 Close();
		 }
private: System::Void browseRouteFileLocation_Click(System::Object^  sender, System::EventArgs^  e) {
			 OpenFileDialog^ sfd = gcnew OpenFileDialog();
             sfd->Filter = "XML Files|*.xml";
             sfd->ShowDialog();
		     CANStructLocation->Text=sfd->FileName;
		 }
private: System::Void routeFileLocation_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("telemetry","canstructure",marshal_as<string>(CANStructLocation->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void browseJarLocation_Click(System::Object^  sender, System::EventArgs^  e) {
			 OpenFileDialog^ sfd = gcnew OpenFileDialog();
             sfd->Filter = "Java Archives|*.jar";
             sfd->ShowDialog();
		     jarLocation->Text=sfd->FileName;
		 }
private: System::Void jarLocation_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("telemetry","jarpath",marshal_as<string>(jarLocation->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
private: System::Void button2_Click(System::Object^  sender, System::EventArgs^  e) {
			 OpenFileDialog^ sfd = gcnew OpenFileDialog();
             sfd->Filter = "Executable Files|*.exe";
             sfd->ShowDialog();
		     javawLocation->Text=sfd->FileName;
		 }
private: System::Void javawLocation_TextChanged(System::Object^  sender, System::EventArgs^  e) {
			 solarConfig->setProperty("java","javapath",marshal_as<string>(javawLocation->Text));
			 save->Enabled=true;
			 toolStripStatusLabel->Text="";
		 }
};
}

