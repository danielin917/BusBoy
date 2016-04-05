#pragma once
#include <map>
#include <string>
#include <fstream>
#include <INI.h>
using namespace std;
namespace TelemLogger {

	using namespace System;
	using namespace System::ComponentModel;
	using namespace System::Collections;
	using namespace System::Windows::Forms;
	using namespace System::Data;
	using namespace System::Drawing;
	using namespace System::IO;
	using namespace System::Xml;
	using namespace System::Xml::Schema;
	using namespace System::Xml::Serialization;
	using namespace System::Text;
	using namespace System::Runtime::InteropServices;
	using namespace System::Collections::Generic;

	/// <summary>
	/// Summary for Form1
	/// </summary>
	public ref class Form1 : public System::Windows::Forms::Form
	{
	public:
		System::String^ getFileName()
		{
			return outfile;
		}
		Form1(System::String^ target,INI* solarConfig)
		{
			this->solarConfig=solarConfig;
			InitializeComponent();
			outfile=nullptr;
			subkeyMap=gcnew Dictionary<System::String^,cli::array< System::Object^  >^>();
			checkedKeys=gcnew Dictionary<System::String^,cli::array< System::String^  >^>();
			frequencies=gcnew Dictionary<System::String^,System::String^>();
			messageList=gcnew cli::array<System::Object^>(0);
			lastSelected=gcnew String("");
			string& str=(*solarConfig)["telemetry"]["canstructure"];
			if(str=="")str="canstructure.xml";
			loadCanStruct(str);
		}
	private: System::Windows::Forms::Button^  button1;

	private: System::Windows::Forms::CheckedListBox^  canmessages;
	private: System::Windows::Forms::GroupBox^  groupBox1;
	private: System::Windows::Forms::Label^  label1;
	private: System::Windows::Forms::ComboBox^  hurts;
	private: System::Windows::Forms::Label^  label2;
	private: System::Windows::Forms::CheckedListBox^  keys;
	public: 
	string Native(System::String^ str)
	{
		IntPtr ptr = System::Runtime::InteropServices::Marshal::StringToHGlobalAnsi(str);
		string ret=(char*)ptr.ToPointer();
		System::Runtime::InteropServices::Marshal::FreeHGlobal(ptr);
		return ret;
	}
	System::String^ getStr(System::Windows::Forms::CheckedListBox^ container, Collections::IEnumerator^ enumer)
	{
		return safe_cast<String^>(container->Items->default[*safe_cast<Int32^>(enumer->Current)]);
	}
	protected:
		INI* solarConfig;
		XmlReader^ canstructure;
		Dictionary<System::String^,cli::array< System::Object^  >^>^ subkeyMap;
		Dictionary<System::String^,cli::array< System::String^  >^>^ checkedKeys;
		Dictionary<System::String^,System::String^>^ frequencies;
		System::String^ lastSelected;
	private: System::Windows::Forms::Button^  saveConfig;
	protected: 
		String^ outfile;
		cli::array<System::Object^>^ messageList;
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		~Form1()
		{
			if (components)
			{
				delete components;
			}
		}

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
			this->button1 = (gcnew System::Windows::Forms::Button());
			this->canmessages = (gcnew System::Windows::Forms::CheckedListBox());
			this->keys = (gcnew System::Windows::Forms::CheckedListBox());
			this->saveConfig = (gcnew System::Windows::Forms::Button());
			this->groupBox1 = (gcnew System::Windows::Forms::GroupBox());
			this->label2 = (gcnew System::Windows::Forms::Label());
			this->label1 = (gcnew System::Windows::Forms::Label());
			this->hurts = (gcnew System::Windows::Forms::ComboBox());
			this->groupBox1->SuspendLayout();
			this->SuspendLayout();
			// 
			// button1
			// 
			this->button1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Left));
			this->button1->Location = System::Drawing::Point(12, 329);
			this->button1->Name = L"button1";
			this->button1->Size = System::Drawing::Size(126, 23);
			this->button1->TabIndex = 0;
			this->button1->Text = L"Load CAN Structure";
			this->button1->UseVisualStyleBackColor = true;
			this->button1->Click += gcnew System::EventHandler(this, &Form1::button1_Click);
			// 
			// canmessages
			// 
			this->canmessages->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Bottom) 
				| System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->canmessages->Enabled = false;
			this->canmessages->FormattingEnabled = true;
			this->canmessages->Location = System::Drawing::Point(6, 19);
			this->canmessages->Name = L"canmessages";
			this->canmessages->Size = System::Drawing::Size(154, 259);
			this->canmessages->TabIndex = 2;
			this->canmessages->SelectedIndexChanged += gcnew System::EventHandler(this, &Form1::canmessages_SelectedIndexChanged);
			// 
			// keys
			// 
			this->keys->Anchor = static_cast<System::Windows::Forms::AnchorStyles>(((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Bottom) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->keys->Enabled = false;
			this->keys->FormattingEnabled = true;
			this->keys->Location = System::Drawing::Point(166, 19);
			this->keys->Name = L"keys";
			this->keys->Size = System::Drawing::Size(158, 259);
			this->keys->TabIndex = 3;
			// 
			// saveConfig
			// 
			this->saveConfig->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->saveConfig->Location = System::Drawing::Point(216, 329);
			this->saveConfig->Name = L"saveConfig";
			this->saveConfig->Size = System::Drawing::Size(131, 23);
			this->saveConfig->TabIndex = 4;
			this->saveConfig->Text = L"Save Configuration";
			this->saveConfig->UseVisualStyleBackColor = true;
			this->saveConfig->Click += gcnew System::EventHandler(this, &Form1::saveConfig_Click);
			// 
			// groupBox1
			// 
			this->groupBox1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((((System::Windows::Forms::AnchorStyles::Top | System::Windows::Forms::AnchorStyles::Bottom) 
				| System::Windows::Forms::AnchorStyles::Left) 
				| System::Windows::Forms::AnchorStyles::Right));
			this->groupBox1->Controls->Add(this->label2);
			this->groupBox1->Controls->Add(this->label1);
			this->groupBox1->Controls->Add(this->hurts);
			this->groupBox1->Controls->Add(this->canmessages);
			this->groupBox1->Controls->Add(this->keys);
			this->groupBox1->Location = System::Drawing::Point(12, 12);
			this->groupBox1->Name = L"groupBox1";
			this->groupBox1->Size = System::Drawing::Size(335, 311);
			this->groupBox1->TabIndex = 5;
			this->groupBox1->TabStop = false;
			this->groupBox1->Text = L"CAN Messages";
			// 
			// label2
			// 
			this->label2->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->label2->AutoSize = true;
			this->label2->Location = System::Drawing::Point(142, 287);
			this->label2->Name = L"label2";
			this->label2->Size = System::Drawing::Size(18, 13);
			this->label2->TabIndex = 8;
			this->label2->Text = L"(s)";
			// 
			// label1
			// 
			this->label1->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->label1->AutoSize = true;
			this->label1->Location = System::Drawing::Point(8, 287);
			this->label1->Name = L"label1";
			this->label1->Size = System::Drawing::Size(37, 13);
			this->label1->TabIndex = 7;
			this->label1->Text = L"Period";
			// 
			// hurts
			// 
			this->hurts->AllowDrop = true;
			this->hurts->Anchor = static_cast<System::Windows::Forms::AnchorStyles>((System::Windows::Forms::AnchorStyles::Bottom | System::Windows::Forms::AnchorStyles::Right));
			this->hurts->FormattingEnabled = true;
			this->hurts->Items->AddRange(gcnew cli::array< System::Object^  >(12) {L"0", L"0.0500", L"0.0625", L"0.125", L"0.25", L"0.5", 
				L"1", L"2", L"4", L"8", L"16", L"20"});
			this->hurts->Location = System::Drawing::Point(51, 284);
			this->hurts->Name = L"hurts";
			this->hurts->Size = System::Drawing::Size(89, 21);
			this->hurts->TabIndex = 6;
			this->hurts->Text = L"0";
			this->hurts->SelectedIndexChanged += gcnew System::EventHandler(this, &Form1::hurts_SelectedIndexChanged);
			// 
			// Form1
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(359, 364);
			this->Controls->Add(this->groupBox1);
			this->Controls->Add(this->saveConfig);
			this->Controls->Add(this->button1);
			this->Name = L"Form1";
			this->Text = L"Telemetry Logging Profile";
			this->Load += gcnew System::EventHandler(this, &Form1::Form1_Load);
			this->groupBox1->ResumeLayout(false);
			this->groupBox1->PerformLayout();
			this->ResumeLayout(false);

		}
#pragma endregion
	private: System::Void Form1_Load(System::Object^  sender, System::EventArgs^  e) {
			 }
	private: System::Void button1_Click(System::Object^  sender, System::EventArgs^  e) {
		OpenFileDialog^ fd=gcnew OpenFileDialog();
		fd->Filter="CAN Structure (*.xml)|*.xml";
		fd->RestoreDirectory=true;
		if ( fd->ShowDialog() == System::Windows::Forms::DialogResult::OK )
		{
			string xmlurl=Native(fd->FileName);
			(*solarConfig)["telemetry"]["canstructure"]=xmlurl;

			loadCanStruct(xmlurl);
		}		
	}
	void loadCanStruct(string& url)
	{
		try{
		canstructure = XmlReader::Create(gcnew String(url.c_str()));
		}
		catch(...)
		{
			canmessages->Enabled=false;
			canmessages->Items->Clear();
			keys->Enabled=false;
			keys->Items->Clear();
			return;
		}
		canmessages->Enabled=true;
		keys->Enabled=true;
		while(canstructure->ReadToFollowing("canmsg"))
        {
			XmlReader^ mstruct=canstructure->ReadSubtree();
			System::String^ msg=nullptr;
			bool found=false;
			bool is_data=false;
			while(canstructure->MoveToNextAttribute())//find the message name
			{
				if(canstructure->Name=="type")
				{
					if(canstructure->Value=="data") is_data=true;
					else break;
				}
				if(canstructure->Name=="id")
				{
					msg=canstructure->Value;
				}
				if(msg && is_data)
				{
					try{
						Array::Resize(messageList, messageList->Length + 1);
						messageList[messageList->Length-1]=msg;
						subkeyMap->Add(msg,gcnew cli::array< System::Object^  >(0));
						checkedKeys->Add(msg,gcnew cli::array< System::String^ >(0));
						found=true;
						break;
					} catch (System::ArgumentException^ e)
					{
						continue;
					}

				}
			}
			if(found)
			while(mstruct->Read())
			{
				while (mstruct->MoveToNextAttribute())
                {
                    if(mstruct->Name=="name")
					{
						//textBox1->Text=mstruct->Value;
						cli::array< System::Object^  >^ arr=subkeyMap[msg];
						Array::Resize(arr, arr->Length + 1);
						arr[arr->Length-1]=mstruct->Value;
						subkeyMap[msg]=arr;
					}
                }
			}
        }
		canmessages->Items->AddRange(messageList);
	}
	private: System::Void canmessages_SelectedIndexChanged(System::Object^  sender, System::EventArgs^  e) {
			System::String^ m=canmessages->SelectedItem->ToString();
			if(frequencies->ContainsKey(m)) hurts->Text=frequencies[m];
			else hurts->Text="0";
			//store the checked indices
			cli::array< System::String^  >^ arr;
			Array::Resize(arr,0);
			Collections::IEnumerator^ myEnum1 = keys->CheckedIndices->GetEnumerator();
			while(myEnum1->MoveNext())
			{
				Array::Resize(arr, arr->Length + 1);
				String^ s=getStr(keys,myEnum1);
				arr[arr->Length-1]=s;
			}
			if(!lastSelected->Equals(""))
				checkedKeys[lastSelected]=arr;
			lastSelected=m;
			keys->Items->Clear();
			Object^ o=subkeyMap[m];
			keys->Items->AddRange(subkeyMap[m]);
			//re-check checked indices
			List<int>^ indicesToCheck=gcnew List<int>();
			for(int i=0;i<checkedKeys[m].Length;++i)
			{
				Collections::IEnumerator^ readder=keys->Items->GetEnumerator();
				int j=0;
				while(readder->MoveNext())
				{
					if(readder->Current->Equals(checkedKeys[m][i]))
					{
						indicesToCheck->Add(j);
					}
					++j;
				}
			}
			for(int i=0;i<indicesToCheck->Count;++i)
			{
				keys->SetItemChecked(indicesToCheck[i],true);
			}

		}
	private: System::Void saveConfig_Click(System::Object^  sender, System::EventArgs^  e) {
			//force saving
			SaveFileDialog^ fd=gcnew SaveFileDialog();
			fd->Filter="Telemetry Monitoring Profiles (*.scttmm)|*.scttmm";
			fd->RestoreDirectory=true;
			if ( fd->ShowDialog() == System::Windows::Forms::DialogResult::OK )
			{
				this->outfile=fd->FileName;
			
			}
			canmessages_SelectedIndexChanged(nullptr,nullptr);
			ofstream outfile(Native(this->outfile));
			Collections::IEnumerator^ outerEnumerator=canmessages->CheckedIndices->GetEnumerator();
			while(outerEnumerator->MoveNext())
			{
				String^ str=getStr(canmessages,outerEnumerator);
				outfile<<Native(str);
				if(frequencies->ContainsKey(str)) 
				{
					outfile<<" "<<Native(frequencies[str]);
				}
				else outfile<<" 0";
				if(checkedKeys[str])
				{
					cli::array<System::String^>^ keyarr=checkedKeys[str];
					for(int i=0;i<keyarr.Length;++i)
					{
						outfile<<" "<<Native(keyarr[i]);
					}
				}
				outfile<<'\n';
			}
		}
private: System::Void hurts_SelectedIndexChanged(System::Object^  sender, System::EventArgs^  e) {
			 if(canmessages->SelectedItem!=nullptr)
			 {
				 String^ s=canmessages->SelectedItem->ToString();
				 frequencies[s]=hurts->Text;
			 }
		 }
};
}

