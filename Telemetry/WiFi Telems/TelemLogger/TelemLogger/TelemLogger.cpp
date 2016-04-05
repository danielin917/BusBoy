// TelemLogger.cpp : main project file.

#include "stdafx.h"
#include "Form1.h"
#include "main_form.h"

using namespace TelemLogger;

[STAThreadAttribute]
int main(array<System::String ^> ^args)
{
	// Enabling Windows XP visual effects before any controls are created
	Application::EnableVisualStyles();
	Application::SetCompatibleTextRenderingDefault(false); 

	// Create the main window and run it
	Application::Run(gcnew main_form());
	return 0;
}
